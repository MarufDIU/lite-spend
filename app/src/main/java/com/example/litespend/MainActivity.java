package com.example.litespend;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private TextView balanceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        db = new DatabaseHelper(this);
        balanceView = findViewById(R.id.balance);
        updateBalance();

        Button addBtn = findViewById(R.id.add_btn);
        Button spendBtn = findViewById(R.id.spend_btn);
        Button statsBtn = findViewById(R.id.stats_btn);

        addBtn.setOnClickListener(v -> showInputDialog("ADD"));
        spendBtn.setOnClickListener(v -> showInputDialog("SPEND"));
        statsBtn.setOnClickListener(v -> showStatistics());
        
        loadRecentTransactions();
    }

    private void showInputDialog(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_input, null);
        EditText amountInput = view.findViewById(R.id.amount);
        EditText noteInput = view.findViewById(R.id.note);

        builder.setView(view)
                .setTitle(type + " Money")
                .setPositiveButton("Save", (d, w) -> {
                    try {
                        double amount = Double.parseDouble(amountInput.getText().toString());
                        String note = noteInput.getText().toString();
                        db.addTransaction(type, amount, note);
                        updateBalance();
                        loadRecentTransactions();
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateBalance() {
        balanceView.setText(String.format("Balance: ₹%.2f", db.getBalance()));
    }

    private void loadRecentTransactions() {
        Cursor cursor = db.getReadableDatabase().rawQuery(
                "SELECT _id, type, amount, note, timestamp FROM transactions ORDER BY timestamp DESC LIMIT 10", null);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.list_item,
                cursor,
                new String[]{"type", "amount", "note", "timestamp"},
                new int[]{R.id.type, R.id.amount, R.id.note, R.id.timestamp},
                0);

        ((ListView)findViewById(R.id.list)).setAdapter(adapter);
    }

    private void showStatistics() {
        StringBuilder stats = new StringBuilder();
        Cursor c = db.getDailyStats();
        
        while(c.moveToNext()) {
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