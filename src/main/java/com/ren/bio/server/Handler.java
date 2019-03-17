package com.ren.bio.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Handler {
    String httpResponse = "HTTP/1.1 200 OK\r\n" + 
            "Content-Length: 24\r\n" + 
            "Access-Control-Allow-Origin: *\r\n" +
            "\r\n" + 
            "{\"name\":\"abc\", \"age\":10}";
    /**
     * 交互次数在这个socket上面, 一个socketProcessor 有一个唯一的handler对象.
     */
    private int time = 0;
    public void process(Socket socket) throws IOException {
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
             // 输入流
             br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             // 输出流
             bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             String line = br.readLine();
             System.out.println("time:"+ time + "recv:" + line);
             bw.write(httpResponse);
             bw.flush();
             time++;
        } catch (IOException e) {
            throw  e;
        } 
    }
}
