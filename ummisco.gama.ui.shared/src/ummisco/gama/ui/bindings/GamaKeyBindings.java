/*********************************************************************************************
 *
 * 'GamaKeyBindings.java, in plugin ummisco.gama.ui.shared, is part of the source code of the GAMA modeling and
 * simulation platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package ummisco.gama.ui.bindings;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import msi.gama.runtime.GAMA;
import ummisco.gama.ui.access.GamlSearchField;
import ummisco.gama.ui.utils.PlatformHelper;
import ummisco.gama.ui.utils.WorkbenchHelper;

/**
 * The purpose of this class is to install global key bindings that can work in any of the contexts of GAMA (incl.
 * fullscreen)
 * 
 * @author drogoul
 *
 */
public class GamaKeyBindings implements Listener {

	public static String SEARCH_STRING = format(SWT.COMMAND + SWT.SHIFT, 'H');
	public static String PLAY_STRING = format(SWT.COMMAND, 'P');
	public static String STEP_STRING = format(SWT.COMMAND + SWT.SHIFT, 'P');
	public static String RELOAD_STRING = format(SWT.COMMAND, 'R');
	public static String RELAUNCH_STRING = format(SWT.COMMAND + SWT.SHIFT, 'R');
	public static String QUIT_STRING = format(SWT.COMMAND + SWT.SHIFT, 'X');

	GamaKeyBindings() {}

	@Override
	public void handleEvent(final Event event) {
		// if (GAMA.getFrontmostController() == null)
		// return;
		if (!ctrl(event))
			return;
		switch (event.keyCode) {
			case 'h':
				if (shift(event)) {
					consume(event);
					GamlSearchField.INSTANCE.search();
				}
				break;
			// Handles START & RELOAD
			case 'p':
				if (shift(event)) {
					consume(event);
					GAMA.stepFrontmostExperiment();
				} else {
					consume(event);
					GAMA.startPauseFrontmostExperiment();
				}
				break;
			// Handles PAUSE & STEP
			case 'r':
				if (shift(event)) {
					consume(event);
					GAMA.relaunchFrontmostExperiment();
				} else {
					consume(event);
					GAMA.reloadFrontmostExperiment();
				}
				break;
			// Handles CLOSE
			case 'x':
				if (shift(event)) {
					consume(event);
					GAMA.closeAllExperiments(true, false);
				}
		}

	}

	private void consume(final Event event) {
		event.doit = false;
		event.type = SWT.None;
	}

	private final static GamaKeyBindings BINDINGS = new GamaKeyBindings();

	public static void install() {
		WorkbenchHelper.run(() -> WorkbenchHelper.getDisplay().addFilter(SWT.KeyDown, BINDINGS));
	}

	public static boolean ctrl(final Event e) {
		return PlatformHelper.isCocoa() ? (e.stateMask & SWT.COMMAND) != 0 : (e.stateMask & SWT.CTRL) != 0;
	}

	public static boolean ctrl(final KeyEvent e) {
		return PlatformHelper.isCocoa() ? (e.stateMask & SWT.COMMAND) != 0 : (e.stateMask & SWT.CTRL) != 0;
	}

	public static boolean ctrl(final MouseEvent e) {
		return PlatformHelper.isCocoa() ? (e.stateMask & SWT.COMMAND) != 0 : (e.stateMask & SWT.CTRL) != 0;
	}

	public static boolean shift(final Event e) {
		return (e.stateMask & SWT.SHIFT) != 0;
	}

	public static boolean shift(final KeyEvent e) {
		return (e.stateMask & SWT.SHIFT) != 0;
	}

	public static boolean shift(final MouseEvent e) {
		return (e.stateMask & SWT.SHIFT) != 0;
	}

	public static String format(final int mod, final int key) {

		return SWTKeySupport.getKeyFormatterForPlatform().format(KeyStroke.getInstance(mod, key));
	}

}