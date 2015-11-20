/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.ibuildapp.romanblack.QRPlugin.result.supplement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.widget.TextView;
import com.ibuildapp.romanblack.QRPlugin.HttpHelper;
import com.ibuildapp.romanblack.QRPlugin.LocaleManager;
import com.ibuildapp.romanblack.QRPlugin.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Handler;

import com.ibuildapp.romanblack.QRPlugin.history.HistoryManager;

final class BookResultInfoRetriever extends SupplementalInfoRetriever {

    private final String isbn;
    private final String source;
    private final Context context;

    BookResultInfoRetriever(TextView textView,
            String isbn,
            Handler handler,
            HistoryManager historyManager,
            Context context) {
        super(textView, handler, historyManager);
        this.isbn = isbn;
        this.source = context.getString(R.string.msg_google_books);
        this.context = context;
    }

    @Override
    void retrieveSupplementalInfo() throws IOException, InterruptedException {

        String contents = HttpHelper.downloadViaHttp("https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn,
                HttpHelper.ContentType.JSON);

        if (contents.length() == 0) {
            return;
        }

        String title;
        String pages;
        Collection<String> authors = null;

        try {

            JSONObject topLevel = (JSONObject) new JSONTokener(contents).nextValue();
            JSONArray items = topLevel.optJSONArray("items");
            if (items == null || items.isNull(0)) {
                return;
            }

            JSONObject volumeInfo = ((JSONObject) items.get(0)).getJSONObject("volumeInfo");
            if (volumeInfo == null) {
                return;
            }

            title = volumeInfo.optString("title");
            pages = volumeInfo.optString("pageCount");

            JSONArray authorsArray = volumeInfo.optJSONArray("authors");
            if (authorsArray != null && !authorsArray.isNull(0)) {
                authors = new ArrayList<String>();
                for (int i = 0; i < authorsArray.length(); i++) {
                    authors.add(authorsArray.getString(i));
                }
            }

        } catch (JSONException e) {
            throw new IOException(e.toString());
        }

        Collection<String> newTexts = new ArrayList<String>();

        if (title != null && title.length() > 0) {
            newTexts.add(title);
        }

        if (authors != null && !authors.isEmpty()) {
            boolean first = true;
            StringBuilder authorsText = new StringBuilder();
            for (String author : authors) {
                if (first) {
                    first = false;
                } else {
                    authorsText.append(", ");
                }
                authorsText.append(author);
            }
            newTexts.add(authorsText.toString());
        }

        if (pages != null && pages.length() > 0) {
            newTexts.add(pages + "pp.");
        }

        String baseBookUri = "http://www.google." + LocaleManager.getBookSearchCountryTLD(context)
                + "/search?tbm=bks&source=zxing&q=";

        append(isbn, source, newTexts.toArray(new String[newTexts.size()]), baseBookUri + isbn);
    }
}