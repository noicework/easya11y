package info.magnolia.form.i18n;

import info.magnolia.form.domain.BaseModel;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.multisite.i18n.MultiSiteI18nAuthoringSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MultiSiteI18nFormsAuthoringSupport<T> extends MultiSiteI18nAuthoringSupport<T> {

    private final SiteManager siteManager;

    public MultiSiteI18nFormsAuthoringSupport(SiteManager siteManager) {
        super(siteManager);
        this.siteManager = siteManager;
    }

    @Override
    public List<Locale> getAvailableLocales(T item) {
        if (item instanceof BaseModel) {
            if (this.isEnabled()) {
                Site site = this.siteManager.getDefaultSite();
                if (site != null && site.getI18n() != null && site.getI18n().isEnabled()) {
                    return new ArrayList<>(site.getI18n().getLocales());
                }
            }
        }
        return super.getAvailableLocales(item);
    }

    @Override
    public Locale getDefaultLocale(T item) {
        if (item instanceof BaseModel) {
            if (this.isEnabled()) {
                Site site = this.siteManager.getDefaultSite();
                if (site != null && site.getI18n() != null && site.getI18n().isEnabled()) {
                    return site.getI18n().getDefaultLocale();
                }
            }
        }
        return super.getDefaultLocale(item);
    }
}