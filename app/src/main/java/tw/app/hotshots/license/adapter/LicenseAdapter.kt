package tw.app.hotshots.license.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import tw.app.hotshots.databinding.ItemLicenseBinding
import tw.app.hotshots.license.model.License

class LicenseAdapter(
    private val context: Context,
    private val listLicenses: ArrayList<License>
) : RecyclerView.Adapter<LicenseAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemLicenseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLicenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            with(listLicenses[position]){
                binding.licenseTitleText.text = this.name
                binding.licenseAuthorText.text = this.author
                binding.licenseVersionText.text = this.version
                binding.licenseDescriptionText.text = this.shortDescription
                binding.licenseIconImageView.setImageResource(this.icon)

                binding.openSiteButton.setOnClickListener {
                    val uriToOpen = Uri.parse(this.url)

                    CustomTabsIntent.Builder()
                        .setStartAnimations(context, android.R.anim.fade_in, android.R.anim.fade_out)
                        .build()
                        .launchUrl(context, uriToOpen)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return listLicenses.size
    }
}