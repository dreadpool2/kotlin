/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.preloading;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CommandLineArgumentsPreprocessor {
    private static final String experimentalArgfileArgument = "-Xargfile";
    private static final char QUOTATION_MARK = '"';
    private static final char BACKSLASH = '\\';
    private static final char WHITESPACE = ' ';
    private static final char NEWLINE = '\n';

    /**
     * Performs initial preprocessing of arguments, passed to the compiler.
     * This is done prior to *any* arguments parsing, and result of preprocessing
     * will be used instead of actual passed arguments.
     */
    public static String[] preprocessArguments(String[] args) {
        List<String> result = new ArrayList<>(args.length);

        for (String arg : args) {
            if (isArgumentForArgfile(arg)) {
                String argfilePath = getArgfilePath(arg);
                File argfile = new File(argfilePath);
                expandArgfile(argfile, result);
            } else {
                result.add(arg);
            }
        }

        String[] arrayResult = new String[result.size()];
        result.toArray(arrayResult);
        return arrayResult;
    }

    private static void expandArgfile(File argfile, List<String> result) {
        try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(argfile)), StandardCharsets.UTF_8)) {
            String next;
            while ((next = parseNextArgument(reader)) != null) {
                result.add(next);
            }
        }
        catch (FileNotFoundException e) {
            // Note that if something that looks like file is actually passed, but
            // then something went wrong, then we just fail immediately.
            // This behaviour is similar to javac.
            throw new Preloader.PreloaderException("File not found: " + argfile.getAbsolutePath());
        }
        catch (IOException e) {
            throw new Preloader.PreloaderException("Error while reading argfile", e);
        }
    }

    private static String parseNextArgument(Reader is) throws IOException {
        StringBuilder sb = new StringBuilder();

        int r;
        while ((r = is.read()) != -1) {
            char ch = (char) r;

            switch (ch) {
                case QUOTATION_MARK:
                    parseRestOfEscapedSequence(is, sb);
                    break;
                case WHITESPACE:
                case NEWLINE:
                    return sb.toString();
                case BACKSLASH:
                    ch = (char) is.read();
                default:
                    sb.append(ch);
                    break;
            }
        }

        if (sb.length() == 0) {
            return null;
        } else {
            return sb.toString();
        }
    }

    private static void parseRestOfEscapedSequence(Reader is, StringBuilder sb) throws IOException {
        char ch;
        while ((ch = (char) is.read()) != QUOTATION_MARK) {
            switch (ch) {
                case BACKSLASH:
                    ch = (char) is.read();
                default:
                    sb.append(ch);
                    break;
            }
        }
    }

    private static String getArgfilePath(String arg) {
        return arg.replaceFirst(experimentalArgfileArgument + "=", "");
    }

    private static boolean isArgumentForArgfile(String arg) {
        // Note that currently we use only experimental syntax for passing argfiles
        // In 1.3 we can support also javac-like syntax `@argfile`
        return arg.startsWith(experimentalArgfileArgument + "=");
    }
}
