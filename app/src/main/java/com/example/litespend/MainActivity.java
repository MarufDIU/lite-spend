package com.example.litespend;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TextView balanceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        balanceView = findViewById(R.id.balance);

        Button addBtn = findViewById(R.id.add_btn);
        Button spendBtn = findViewById(R.id.spend_btn);
        Button statsBtn = findViewById(R.id.stats_btn);

        updateBalance();
        loadTransactions();

        addBtn.setOnClickListener(v -> showTransactionDialog("ADD"));
        spendBtn.setOnClickListener(v -> showTransactionDialog("SPEND"));
        statsBtn.setOnClickListener(v -> showStatistics());
    }

    private void showTransactionDialog(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_input, null);
        EditText amountInput = view.findViewById(R.id.amount);
        EditText noteInput = view.findViewById(R.id.note);

        builder.setView(view)
                .setTitle(type + " Entry")
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        double amount = Double.parseDouble(amountInput.getText().toString());
                        String note = noteInput.getText().toString();
                        dbHelper.addTransaction(type, amount, note);
                        updateBalance();
                        loadTransactions();
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateBalance() {
        balanceView.setText(String.format("Balance: ₹%.2f", dbHelper.getBalance()));
    }

    private void loadTransactions() {
        Cursor cursor = dbHelper.getAllTransactions();
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.list_item,
                cursor,
                new String[]{"type", "amount", "note", "timestamp"},
                new int[]{R.id.type, R.id.amount, R.id.note, R.id.timestamp},
                0);
        ((ListView) findViewById(R.id.list)).setAdapter(adapter);
    }

    private void showStatistics() {
        StringBuilder stats = new StringBuilder();
        Cursor c = dbHelper.getDailyStats();

        while (c.moveToNext()) {
            stats.append(c.getString(0))
                    .append("\nSpent: ₹").append(c.getDouble(1))
                    .append("\nEarned: ₹").append(c.getDouble(2))
                    .append("\n\n");
        }
        c.close();

        new AlertDialog.Builder(this)
                .setTitle("30-Day Statistics")
                .setMessage(stats.toString())
                .setPositiveButton("OK", null)
                .show();
    }
}