// camel-k: trait=knative-service.min-scale=0
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acme.s3.gsheets;

import java.util.Arrays;

import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;

import com.google.api.services.sheets.v4.model.ValueRange;

/**
 * Camel route definitions.
 */
public class S3ToSheets extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("aws2-s3:{{aws-s3.bucket-name}}")
                .log("${body}")
                .process(exchange -> {
                    final Message m = exchange.getMessage();
                    m.setHeader("CamelGoogleSheets.valueInputOption", "RAW");
                    m.setHeader("CamelGoogleSheets.values",
                            new ValueRange().setValues(
                                    Arrays.asList(
                                            Arrays.asList(
                                                    m.getBody(String.class)))));

                })
                // Throttle to avoid exceeding the default Google API limit of 60 requests per min
                .throttle(1).timePeriodMillis(1500)
                .to("google-sheets://data/append?spreadsheetId={{google-sheets.spreadsheet-id}}&range=Sheet1!A1:A1");
    }

}