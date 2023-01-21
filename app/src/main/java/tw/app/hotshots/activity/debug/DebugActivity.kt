package tw.app.hotshots.activity.debug

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import tw.app.hotshots.adapter.recyclerview.LogsAdapter
import tw.app.hotshots.databinding.ActivityDebugBinding
import tw.app.hotshots.logger.Logger

class DebugActivity : AppCompatActivity() {

    private var _binding: ActivityDebugBinding? = null
    private val binding get() = _binding!!

    lateinit var adapter: LogsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityDebugBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()
    }

    private fun setup() {
        val linearManager = LinearLayoutManager(this@DebugActivity)
        binding.logsRecyclerView.layoutManager = linearManager

        adapter = LogsAdapter(Logger.getLogs())
        binding.logsRecyclerView.adapter = adapter

        binding.clearLogsButton.setOnClickListener {
            adapter = LogsAdapter(arrayListOf())
            Logger.clearLogs()
            binding.logsRecyclerView.adapter = adapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}