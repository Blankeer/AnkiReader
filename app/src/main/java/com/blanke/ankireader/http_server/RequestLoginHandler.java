package com.blanke.ankireader.http_server;

import com.blanke.ankireader.bean.Note;
import com.blanke.ankireader.play.PlayerService;
import com.yanzhenjie.andserver.RequestHandler;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import java.io.UnsupportedEncodingException;

public class RequestLoginHandler implements RequestHandler {
    @Override
    public void handle(HttpRequest req, HttpResponse response, HttpContext con) {
        StringEntity stringEntity = null;
        Note note = PlayerService.currentNote;
        try {
            String text = "null";
            if (note != null) {
                text = note.getFront().toString();
            }
            stringEntity = new StringEntity(text, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.setEntity(stringEntity);
    }
}