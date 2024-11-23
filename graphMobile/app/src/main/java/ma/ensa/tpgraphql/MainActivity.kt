package ma.ensa.tpgraphql

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import ma.ensa.tpgraphql.adapter.CompteAdapter
import ma.ensa.tpgraphql.type.TypeCompte
import ma.ensa.tpgraphql.viewmodel.CompteViewModel

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var viewModel: CompteViewModel
    private lateinit var adapter: CompteAdapter
    private lateinit var listView: RecyclerView
    private lateinit var btnAdd: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "MainActivity created")

        setupViews()
        setupViewModel()
        setupObservers()

        viewModel.fetchComptes()
    }

    private fun setupViews() {
        listView = findViewById(R.id.listView)
        btnAdd = findViewById(R.id.btnAdd)

        adapter = CompteAdapter()
        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = adapter

        btnAdd.setOnClickListener {
            showAddDialog()
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[CompteViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.comptes.observe(this) { elements ->
            Log.d(TAG, "Reçu ${elements.size} éléments")
            adapter.updateData(elements)
        }

        viewModel.error.observe(this) { erreur ->
            Log.e(TAG, "Erreur reçue: $erreur")
            Toast.makeText(this, "Erreur: $erreur", Toast.LENGTH_LONG).show()
        }

        viewModel.totalComptes.observe(this) { total ->
            findViewById<TextView>(R.id.txtTotalAccounts).text = "Nombre total: $total"
        }

        viewModel.totalSolde.observe(this) { total ->
            findViewById<TextView>(R.id.txtTotalBalance).text = "Montant total: ${total} DH"
        }
    }

    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_compte, null)
        val montantInput = dialogView.findViewById<EditText>(R.id.editMontant)  // Changé à EditText
        val typeSpinner = dialogView.findViewById<Spinner>(R.id.spinnerCategorie)

        val validTypes = TypeCompte.knownValues()
            .map { it.rawValue }

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, validTypes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = spinnerAdapter

        MaterialAlertDialogBuilder(this)
            .setTitle("Nouvel élément")
            .setView(dialogView)
            .setPositiveButton("Valider") { dialog, _ ->
                val montantText = montantInput.text.toString()
                val typeSelected = typeSpinner.selectedItem.toString()

                if (montantText.isNotEmpty() && typeSelected.isNotEmpty()) {
                    try {
                        val montant = montantText.toDouble()
                        val type = TypeCompte.safeValueOf(typeSelected)
                        viewModel.addCompte(montant, type)
                    } catch (e: NumberFormatException) {
                        Toast.makeText(this, "Montant invalide", Toast.LENGTH_SHORT).show()
                    } catch (e: IllegalArgumentException) {
                        Toast.makeText(this, "Type invalide", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }}