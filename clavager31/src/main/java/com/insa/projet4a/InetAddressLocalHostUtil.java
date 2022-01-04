package com.insa.projet4a;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Utility to improve the somewhat broken {@link InetAddress#getLocalHost()}.
 *
 * @see <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4665037">JDK Bug 4665037</a>
 * @see <a href="https://issues.apache.org/jira/browse/JCS-40">JCS-40</a>
 * @see <a href="https://ops4j1.jira.com/browse/PAXEXAM-740">PAXEXAM-740</a>
 * @see <a href="https://bugs.opendaylight.org/show_bug.cgi?id=8176">ODL-8176</a>
 *
 * @author Michael Vorburger.ch (copy/paste from JCS-40)
 */
public final class InetAddressLocalHostUtil {

    private InetAddressLocalHostUtil() { }

    /**
     * Returns first site local address of the local host, which is most likely
     * the host's LAN IP address.
     * <p/>
     * Contrary to {@link InetAddress#getLocalHost()}, this does not use the
     * name of the host from the system and then resolving that name into
     * address, because that is known to be broken on at least some Linux
     * distributions.
     *
     * This is thus intended for use as a replacement of JDK method
     * <code>InetAddress.getLocalHost</code>, because that method is ambiguous
     * on Linux systems. Linux systems enumerate the loopback network interface
     * the same way as regular LAN network interfaces, but the JDK
     * <code>InetAddress.getLocalHost</code> method does not specify the
     * algorithm used to select the address returned under such circumstances,
     * and will often return the loopback address, which is not valid for
     * network communication. Details <a href=
     * "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">JDK Bug #4665037</a>.
     * <p/>
     * This method will scan all IP addresses on all network interfaces on the
     * host machine to determine the IP address most likely to be the machine's
     * LAN address. If the machine has multiple IP addresses, this method will
     * prefer a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, usually
     * IPv4) if the machine has one (and will return the first site-local
     * address if the machine has more than one), but if the machine does not
     * hold a site-local address, this method will return simply the first
     * non-loopback address found (IPv4 or IPv6).
     * <p/>
     * If this method cannot find a non-loopback address using this selection
     * algorithm, it will fall back to calling and returning the result of JDK
     * method <code>InetAddress.getLocalHost</code>.
     *
     * @exception UnknownHostException
     *                if the local host name could not be resolved into an
     *                address.
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    public static InetAddress getLocalHost() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = ifaces.nextElement();
                // Iterate all IP addresses assigned to each card...
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // Found non-loopback site-local address. Return it immediately...
                            return inetAddr;
                        }
                        else if (candidateAddress == null) {
                            // Found non-loopback address, but not necessarily site-local.
                            // Store it as a candidate to be returned if site-local address is not subsequently found...
                            // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                            // only the first. For subsequent iterations, candidateAddress will be non-null.
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                // We did not find a site-local address, but we found some other non-loopback address.
                // Server might have a non-site-local address assigned to its NIC
                // (or it might be running IPv6 which deprecates the "site-local" concept)
                // So return this non-loopback candidate address...
                return candidateAddress;
            }
            // At this point, we did not find a non-loopback address.
            // So fall back to returning whatever InetAddress.getLocalHost() returns...
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        }
        catch (Exception e) { // CHECKSTYLE:SKIP
            UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e.getMessage());
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

    public static String getLocalHostAsString() throws UnknownHostException {
        return getLocalHost().getHostAddress();
    }
}