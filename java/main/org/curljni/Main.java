package org.curljni;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class Main {

    public static void main(String... args) throws IOException {
        try (CurlEasy c = CurlEasy.make();
             Headers hh = Headers.create(Map.of("foo", "bar"));
             FILE wf = FILE.open("aaa.txt", "wb");
             OutputStream out = new ByteArrayOutputStream(32);
             Accumulator acc = Accumulator.create(2048);
             WriteStream ws = WriteStream.create(out);
             WriteCallback wc = WriteCallback.create(bb -> {
                 System.out.println(bb);
             })
        ) {
            c
                .resetOptions()
                // .setWriteCallback(wc)
                // .setWriteFile(wf)
                .setWriteStream(ws);
                // .setPostData(new byte[] {1, 2, 3})

            c.perform();
            System.out.println(out.toString().substring(0, 5));

            // System.out.println(acc.getString());
//            System.out.println(c.getResponseCode());
//            System.out.println(c.getConnectTimeMs());
//            System.out.println(c.getContentLengthDownload());
//            System.out.println(c.getRedirectTime());
//            System.out.println(c.getOsErrno());
//            System.out.println(c.getLocalIP());
//            System.out.println(c.getLocalPort());
//            System.out.println(c.getPrimaryIP());
//            System.out.println(c.getPrimaryPort());
//            System.out.println(c.getEffectiveURL());
            System.out.println(c.getHeaders());
        }
    }
}
