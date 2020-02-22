/*
    This file is part of AMConnectionFactory.

    AMConnectionFactory is free software: you can redistribute it and/or modify
    it under the terms of the GNU LESSER General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AMConnectionFactory is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AMConnectionFactory.  If not, see <https://www.gnu.org/licenses/>.
 */
package am.server.tasks;

import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Callable;

import am.server.AMLibraryFactory;
import am.server.client.AMLibraryRemote;

public class ShutdownProcess implements Callable<Void> {

	private final AMLibraryRemote engine;

	private final Registry registry;

	public ShutdownProcess(Registry registry, AMLibraryRemote engine) {
		super();
		this.registry = registry;
		this.engine = engine;
	}

	@Override
	public Void call() throws Exception {

		registry.unbind(engine.getClass().getName());

		UnicastRemoteObject.unexportObject(engine, true);

		AMLibraryFactory.getInstance().AmCleanup();

		return null;

	}

}
