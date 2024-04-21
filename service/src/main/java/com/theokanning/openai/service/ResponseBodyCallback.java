package com.theokanning.openai.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiError;
import com.theokanning.openai.OpenAiHttpException;

import io.reactivex.FlowableEmitter;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * Callback to parse Server Sent Events (SSE) from raw InputStream and
 * emit the events with io.reactivex.FlowableEmitter to allow streaming of
 * SSE.
 */
public class ResponseBodyCallback implements Callback<ResponseBody> {
    private static final ObjectMapper mapper = OpenAiService.defaultObjectMapper();

    private FlowableEmitter<SSE> emitter;
    private boolean emitDone;

    public ResponseBodyCallback(FlowableEmitter<SSE> emitter, boolean emitDone) {
        this.emitter = emitter;
        this.emitDone = emitDone;
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        // Initialize a BufferedReader to null
        BufferedReader reader = null;

        try {
            // Check if the response is not successful
            if (!response.isSuccessful()) {
                // Create a new HttpException with the response
                HttpException e = new HttpException(response);
                // Get the error body from the response
                ResponseBody errorBody = response.errorBody();

                // If the error body is null, throw the HttpException
                if (errorBody == null) {
                    throw e;
                } else {
                    // Otherwise, read the error body into an OpenAiError object and throw a new OpenAiHttpException
                    OpenAiError error = mapper.readValue(
                            errorBody.string(),
                            OpenAiError.class
                    );
                    throw new OpenAiHttpException(error, e, e.code());
                }
            }

            // Get the InputStream from the response body
            InputStream in = response.body().byteStream();
            // Initialize the BufferedReader with the InputStream
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            // Initialize variables for line reading and SSE parsing
            String line;
            SSE sse = null;
            boolean sseException = false;
            String sseExceptionLine = "";

            // Read lines from the BufferedReader until the emitter is cancelled or there are no more lines
            while (!emitter.isCancelled() && (line = reader.readLine()) != null) {
                // If the line starts with "data:", parse it into an SSE object
                if (line.startsWith("data:")) {
                    sse = new SSE(line.substring(5).trim());
                    // If an SSE exception occurred previously, reset the flag
                    if (sseException) sseException = false;
                } else if (line.isEmpty() && sse != null) {
                    // If the line is empty and an SSE object exists, check if it's done
                    if (sse.isDone()) {
                        // If the SSE is done and emitDone is true, emit the SSE and break the loop
                        if (emitDone) {
                            emitter.onNext(sse);
                        }
                        break;
                    }
                    // If the SSE is not done, emit it and reset the SSE object
                    emitter.onNext(sse);
                    sse = null;
                } else {
                    // If the line does not start with "data:" and is not empty, set the SSE exception flag and store the line
                    sseException = true;
                    sseExceptionLine = line;
                }
            }

            // If an SSE exception occurred, throw an SSEFormatException
            if (sseException) {
                throw new SSEFormatException("Invalid sse format! " + sseExceptionLine);
            }

            // Complete the emitter
            emitter.onComplete();

        } catch (Throwable t) {
            // If any exception occurs, call the onFailure method
            onFailure(call, t);
        } finally {
            // Close the BufferedReader if it was initialized
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                    // Ignore any exceptions when closing the BufferedReader
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        emitter.onError(t);
    }
}