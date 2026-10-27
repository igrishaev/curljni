package org.curljni;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Main {

    public static void main(String... args) throws IOException {
        try (CurlEasy c = CurlEasy.make();
             Headers hh = Headers.create(new String[] {"foo: bar"});
             FILE wf = FILE.open("aaa.txt", "wb");
             OutputStream out = new ByteArrayOutputStream(32);
             Accumulator acc = Accumulator.create(2048);
             WriteStream ws = WriteStream.create(out)
        ) {
            c
                .resetOptions()
                .setUrl("https://habr.com")
                .setProxy(null)
                .setHeaders(hh)
                .setUserAgent(null)
                .setFollowLocation(3)
                .setAccumulator(acc)
                .setServerResponseTimeoutMs(5000)
                // .setTimeoutMs(5)
                // .setConnectTimeoutMs(5)
                // .setWriteFile(wf)
                // .setWriteStream(ws)
                .setMethod(1);
                // .setVerbose(true);
                // .setPostData(new byte[] {1, 2, 3})

            System.out.println(Foo.getFoo(333));

            try {
                c.perform();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            // System.out.println(acc.getString());
            System.out.println(c.getResponseCode());
            System.out.println(c.getConnectTimeMs());
            System.out.println(c.getContentLengthDownload());
            System.out.println(c.getRedirectTime());
            System.out.println(c.getOsErrno());
            System.out.println(c.getLocalIP());
            System.out.println(c.getLocalPort());
            System.out.println(c.getPrimaryIP());
            System.out.println(c.getPrimaryPort());
            System.out.println(c.getEffectiveURL());
            System.out.println(c.getHeaders());
        }
    }
}
