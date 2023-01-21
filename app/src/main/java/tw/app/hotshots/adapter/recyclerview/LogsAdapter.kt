package tw.app.hotshots.adapter.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tw.app.hotshots.databinding.ItemLogCriticalBinding
import tw.app.hotshots.databinding.ItemLogErrorBinding
import tw.app.hotshots.databinding.ItemLogInitBinding
import tw.app.hotshots.databinding.ItemLogNormalBinding
import tw.app.hotshots.logger.LogType
import tw.app.hotshots.logger.model.Log
import tw.app.hotshots.util.TimeUtil

class LogsAdapter(
    private val mData: MutableList<Log>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == LogType.INITIALIZATION.getValue()) {
            InitializationViewHolder.from(parent)
        } else if (viewType == LogType.NORMAL.getValue()) {
            NormalViewHolder.from(parent)
        } else if (viewType == LogType.CRITICAL.getValue()) {
            CriticalViewHolder.from(parent)
        } else {
            ErrorViewHolder.from(parent)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is InitializationViewHolder -> holder.bind(position, mData[position])
            is NormalViewHolder -> holder.bind(position, mData[position])
            is CriticalViewHolder -> holder.bind(position, mData[position])
            is ErrorViewHolder -> holder.bind(position, mData[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mData[position].type.getValue()
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    class InitializationViewHolder private constructor(val binding: ItemLogInitBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, log: Log) {
            with(binding.root) {
                binding.logMessageText.text = log.message
                binding.logCreatedAtText.text = TimeUtil.convertLongToTime(log.createdAt)
            }
        }

        companion object {
            fun from(parent: ViewGroup): InitializationViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemLogInitBinding.inflate(layoutInflater, parent, false)
                return InitializationViewHolder(binding)
            }
        }
    }

    class NormalViewHolder private constructor(val binding: ItemLogNormalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, log: Log) {
            with(binding.root) {
                binding.logMessageText.text = log.message
                binding.logCreatedAtText.text = TimeUtil.convertLongToTime(log.createdAt)
            }
        }

        companion object {
            fun from(parent: ViewGroup): NormalViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemLogNormalBinding.inflate(layoutInflater, parent, false)
                return NormalViewHolder(binding)
            }
        }
    }

    class ErrorViewHolder private constructor(val binding: ItemLogErrorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, log: Log) {
            with(binding.root) {
                binding.logMessageText.text = log.message
                binding.logCreatedAtText.text = TimeUtil.convertLongToTime(log.createdAt)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ErrorViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemLogErrorBinding.inflate(layoutInflater, parent, false)
                return ErrorViewHolder(binding)
            }
        }
    }

    class CriticalViewHolder private constructor(val binding: ItemLogCriticalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, log: Log) {
            with(binding.root) {
                binding.logMessageText.text = log.message
                binding.logCreatedAtText.text = TimeUtil.convertLongToTime(log.createdAt)
            }
        }

        companion object {
            fun from(parent: ViewGroup): CriticalViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemLogCriticalBinding.inflate(layoutInflater, parent, false)
                return CriticalViewHolder(binding)
            }
        }
    }
}