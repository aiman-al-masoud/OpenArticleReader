package com.luxlunaris.openarticlereader.ui;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.luxlunaris.openarticlereader.R;


public class TextPromptDialog extends DialogFragment {


    EditText textField;
    Button confirmButton;

    TextRequester listener;

    String tag;

    String userText;

    public TextPromptDialog() {
        // Required empty public constructor
    }


    public static TextPromptDialog newInstance() {
        TextPromptDialog fragment = new TextPromptDialog();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_text_prompt_dialog, container, false);

        confirmButton = view.findViewById(R.id.confirm_text_prompt_button);
        textField = view.findViewById(R.id.enter_text_prompt);

        confirmButton.setOnClickListener(new HandleConfirm());

        return view;
    }


    public void setPrompt(String tag, String userText){
        this.tag = tag;
        this.userText = userText;
    }


    class HandleConfirm implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            listener.onTextInputted(tag, textField.getText().toString());
            dismiss();
        }
    }


    public void setListener(TextRequester listener){
        this.listener = listener;
    }


    public interface TextRequester{
        public void onTextInputted(String tag, String userResponse);
    }



}