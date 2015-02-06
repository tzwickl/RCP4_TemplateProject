package templateproject.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MDialog;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.service.prefs.BackingStoreException;

/**
 * This class loads and persists the preference store and keeps track of all
 * changes. Furthermore it provides a user interface for displaying and changing
 * preferences.
 * 
 * @author zwickl
 *
 */
@SuppressWarnings("restriction")
public final class PreferencesHandler {
	/**
	 * ID of the preference store.
	 */
	public static final String preferenceStore = "templateProject.application.preferences";

	/**
	 * ID of the preference page as defined in the application model.
	 */
	public static final String preferenceDialog = "templateProject.application.preferences.preferenceDialog";

	/**
	 * Model service.
	 */
	@Inject
	private EModelService modelService;

	/**
	 * Application.
	 */
	@Inject
	private MApplication application;

	/**
	 * Reference to the eclipse global event broker bus.
	 */
	@Inject
	private IEventBroker eventBroker;

	/**
	 * The application's workbench location.
	 */
	@Inject
	@Named(E4Workbench.INSTANCE_LOCATION)
	private Location workspaceLocation;

	/**
	 * Opens the dialog and all its parts.
	 * 
	 * @param prefs
	 *            Reference to the Preference Store.
	 */
	@Execute
	public void showPreferenceStore(@Preference(nodePath = preferenceStore) final IEclipsePreferences prefs) {
		// Sets the dialog and their parts to be rendered.
		List<MDialog> dialogs = this.modelService.findElements(this.application, preferenceDialog, MDialog.class, null);
		MDialog dialog = dialogs.get(0);
		dialog.setToBeRendered(true);

		List<String> tags = new ArrayList<String>(Arrays.asList("Preferences"));
		List<MPartStack> partStack = this.modelService.findElements(dialog, null, MPartStack.class, tags);
		List<MStackElement> parts = partStack.get(0).getChildren();
		for (MStackElement part : parts) {
			part.setToBeRendered(true);
		}
	}

	/**
	 * Sets all default values at the first start-up of the application and
	 * persists them.
	 * 
	 * @param prefs
	 *            Preference Store.
	 */
	@Inject
	@Optional
	public void initializePrefStore(@Preference(nodePath = preferenceStore) final IEclipsePreferences prefs) {
		// Checks if the preference store is already initialized
		if (!prefs.getBoolean("setDefault", true)) {
			return;
		}

		// Indicates that the preference store has already been initialized
		prefs.putBoolean("setDefault", false);
		
		prefs.put(PreferenceKey.LANGUAGE, DefaultSettings.DEFAULT_LANGUAGE);

		// Persists
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tracks the value of {@link PreferenceKey#LANGUAGE} and is invoked
	 * whenever the value of {@link PreferenceKey#LANGUAGE} changes and
	 * saves it in the configuration.
	 * 
	 * @param language
	 *            The new value of {@link PreferenceKey#LANGUAGE}.
	 */
	@Inject
	@Optional
	public void trackWorkspaceLoc(
			@Preference(nodePath = preferenceStore, value = PreferenceKey.LANGUAGE) final String language) {
		Configuration.language = language;
	}
}