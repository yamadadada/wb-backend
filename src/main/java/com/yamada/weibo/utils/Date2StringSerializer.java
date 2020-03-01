package com.yamada.weibo.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Date2StringSerializer extends JsonSerializer<Date> {

    @Override
    public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (calendar.get(Calendar.YEAR) == year) {
            SimpleDateFormat sdf = new SimpleDateFormat("M-dd H:mm");
            jsonGenerator.writeString(sdf.format(date));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd H:mm");
            jsonGenerator.writeString(sdf.format(date));
        }
    }
}
