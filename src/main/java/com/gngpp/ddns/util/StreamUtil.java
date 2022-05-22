/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 gngpp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gngpp.ddns.util;


import com.gngpp.ddns.annotation.Nullable;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author mac
 * @date 2020/7/7
 */
public abstract class StreamUtil {
    public static final int BUFFER_SIZE = 4096;
    private static final byte[] EMPTY_CONTENT = new byte[0];

    public StreamUtil() {
    }

    public static byte[] copyToByteArray(@Nullable InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
            copy(in, out);
            return out.toByteArray();
        }
    }

    public static String copyToString(@Nullable InputStream in, Charset charset) throws IOException {
        if (in == null) {
            return "";
        } else {
            StringBuilder out = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(in, charset);
            char[] buffer = new char[4096];

            int bytesRead;
            while((bytesRead = reader.read(buffer)) != -1) {
                out.append(buffer, 0, bytesRead);
            }

            return out.toString();
        }
    }

    public static String copyToString(ByteArrayOutputStream baos, Charset charset) {
        Assert.notNull(baos, "No ByteArrayOutputStream specified");
        Assert.notNull(charset, "No Charset specified"); try { return baos.toString(charset.name()); } catch (UnsupportedEncodingException var3) { throw new RuntimeException("Failed to copy contents of ByteArrayOutputStream into a String", var3);
        }
    }

    public static void copy(byte[] in, OutputStream out) throws IOException {
        Assert.notNull(in, "No input byte array specified");
        Assert.notNull(out, "No OutputStream specified");
        out.write(in);
    }

    public static void copy(String in, Charset charset, OutputStream out) throws IOException {
        Assert.notNull(in, "No input String specified");
        Assert.notNull(charset, "No Charset specified");
        Assert.notNull(out, "No OutputStream specified");
        Writer writer = new OutputStreamWriter(out, charset);
        writer.write(in);
        writer.flush();
    }

    public static int copy(InputStream in, OutputStream out) throws IOException {
        Assert.notNull(in, "No InputStream specified");
        Assert.notNull(out, "No OutputStream specified");
        int byteCount = 0;
        byte[] buffer = new byte[4096];

        int bytesRead;
        for(; (bytesRead = in.read(buffer)) != -1; byteCount += bytesRead) {
            out.write(buffer, 0, bytesRead);
        }

        out.flush();
        return byteCount;
    }

    public static long copyRange(InputStream in, OutputStream out, long start, long end) throws IOException {
        Assert.notNull(in, "No InputStream specified");
        Assert.notNull(out, "No OutputStream specified");
        long skipped = in.skip(start);
        if (skipped < start) {
            throw new IOException("Skipped only " + skipped + " bytes out of " + start + " required");
        } else {
            long bytesToCopy = end - start + 1L;
            byte[] buffer = new byte[(int)Math.min(4096L, bytesToCopy)];

            while(bytesToCopy > 0L) {
                int bytesRead = in.read(buffer);
                if (bytesRead == -1) {
                    break;
                }

                if ((long)bytesRead <= bytesToCopy) {
                    out.write(buffer, 0, bytesRead);
                    bytesToCopy -= bytesRead;
                } else {
                    out.write(buffer, 0, (int)bytesToCopy);
                    bytesToCopy = 0L;
                }
            }

            return end - start + 1L - bytesToCopy;
        }
    }


    public static InputStream emptyInput() {
        return new ByteArrayInputStream(EMPTY_CONTENT);
    }

    public static InputStream nonClosing(InputStream in) {
        Assert.notNull(in, "No InputStream specified");
        return new StreamUtil.NonClosingInputStream(in);
    }

    public static OutputStream nonClosing(OutputStream out) {
        Assert.notNull(out, "No OutputStream specified");
        return new StreamUtil.NonClosingOutputStream(out);
    }

    private static class NonClosingOutputStream extends FilterOutputStream {
        public NonClosingOutputStream(OutputStream out) {
            super(out);
        }

        public void write(byte[] b, int off, int let) throws IOException {
            this.out.write(b, off, let);
        }

        public void close() throws IOException {
        }
    }

    private static class NonClosingInputStream extends FilterInputStream {
        public NonClosingInputStream(InputStream in) {
            super(in);
        }

        public void close() throws IOException {
        }
    }
}
