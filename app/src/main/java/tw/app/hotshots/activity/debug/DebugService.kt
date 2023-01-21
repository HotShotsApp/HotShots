package tw.app.hotshots.activity.debug

import android.app.Notification
import android.view.LayoutInflater
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.torrydo.floatingbubbleview.ExpandableView
import com.torrydo.floatingbubbleview.FloatingBubble
import com.torrydo.floatingbubbleview.FloatingBubbleService
import tw.app.hotshots.R
import tw.app.hotshots.adapter.recyclerview.LogsAdapter
import tw.app.hotshots.databinding.ActivityDebugBinding
import tw.app.hotshots.databinding.ActivityDebugSupportBinding
import tw.app.hotshots.logger.Logger

class DebugService : FloatingBubbleService() {

    override fun setupNotificationBuilder(channelId: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_bug_report_wb)
            .setContentTitle("HotShots")
            .setContentText("Debug Mode")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    override fun setupBubble(action: FloatingBubble.Action): FloatingBubble.Builder {
        return FloatingBubble.Builder(this)
            // set bubble icon, currently accept only drawable and bitmap
            .setBubble(R.drawable.ic_bug_report_wb)
            // set bubble's width/height
            .setBubbleSizeDp(60, 60)
            // set start point of bubble, (x=0, y=0) is top-left
            .setStartPoint(0, 0)
            // enable auto animate bubble to the left/right side when release, true by default
            .enableAnimateToEdge(true)
            // set close-bubble icon, currently accept only drawable and bitmap
            .setCloseBubble(R.drawable.ic_close_wb)
            // set close-bubble's width/height
            .setCloseBubbleSizeDp(60, 60)
            // set style for close-bubble, null by default
            .setCloseBubbleStyle(null)
            // show close-bubble, true by default
            .enableCloseIcon(true)
            // enable bottom background, false by default
            .bottomBackground(true)
            .addFloatingBubbleTouchListener(object : FloatingBubble.TouchEvent {
                override fun onDestroy() {

                }

                override fun onClick() {
                    action.navigateToExpandableView() // must override `setupExpandableView`, otherwise throw an exception
                }

                override fun onMove(x: Int, y: Int) {

                }

                override fun onUp(x: Int, y: Int) {

                }

                override fun onDown(x: Int, y: Int) {

                }
            })
            .setAlpha(1f)
    }

    override fun setupExpandableView(action: ExpandableView.Action): ExpandableView.Builder? {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ActivityDebugSupportBinding.inflate(inflater)

        binding.closeButton.setOnClickListener {
            action.popToBubble()
        }

        val linearManager = LinearLayoutManager(this)
        binding.logsRecyclerView.layoutManager = linearManager

        var adapter = LogsAdapter(Logger.getLogs())
        binding.logsRecyclerView.adapter = adapter

        binding.clearLogsButton.setOnClickListener {
            adapter = LogsAdapter(arrayListOf())
            Logger.clearLogs()
            binding.logsRecyclerView.adapter = adapter
        }

        binding.refreshButton.setOnClickListener {
            adapter = LogsAdapter(Logger.refresh())
            binding.logsRecyclerView.adapter = adapter
        }

        return ExpandableView.Builder(this)
            .setExpandableView(binding.root)
            .setDimAmount(0.8f)
            // set style for expandable view, fade animation by default
            .addExpandableViewListener(object : ExpandableView.Action {

                override fun popToBubble() {
                    action.popToBubble()
                }

                override fun onOpenExpandableView() {}

                override fun onCloseExpandableView() {}
            })
    }
}