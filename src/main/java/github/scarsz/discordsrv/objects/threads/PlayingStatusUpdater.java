/*
 * DiscordSRV - A Minecraft to Discord and back link plugin
 * Copyright (C) 2016-2019 Austin "Scarsz" Shapiro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package github.scarsz.discordsrv.objects.threads;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayingStatusUpdater extends Thread {
    public PlayingStatusUpdater() {
        setName("DiscordSRV - Status Updater");
    }

    private int lastStatus=0;
    @Override
    public void run() {
        while (true) {
            int rate;
            //Breaking change to config
            try {
                rate=DiscordSRV.config().getInt("StatusUpdateRateInMinutes");
            } catch(IllegalArgumentException e){
                DiscordSRV.error("\"StatusUpdaterRateInMinutes\" not found in config");
                rate=2;
            }

            if (rate < 1) rate = 1;

            if (DiscordUtil.getJda() != null) {
                String status="";
                String rawInput = DiscordSRV.config().getString("DiscordGameStatus");

                if (rawInput.startsWith("[") && rawInput.endsWith("]")) {
                    // Probably a list
                    try {
                        List<String> statuses = DiscordSRV.config().getStringList("DiscordGameStatus");
                        status = statuses.get(lastStatus);

                        // Increment and wrap around
                        lastStatus++;
                        if (lastStatus == statuses.size() - 1)
                            lastStatus = 0;
                    } catch (Exception e){
                        DiscordSRV.error("Something went wrong while loading DiscordGameStatus from config. Is your JSON valid?");
                        e.printStackTrace();
                    }

                } else {
                    if (!StringUtils.isEmpty(rawInput))
                        DiscordSRV.warning("DiscordGameStatus of \""+rawInput+"\" is not in JSON list format. Please wrap it in \"[]\" e.g. [\"Minecraft\"]");
                    status=rawInput;
                }
                if(!StringUtils.isEmpty(status))
                    DiscordUtil.setGameStatus(status);
                else
                    DiscordSRV.debug("Skipping status update cycle, status was null");
            } else {
                DiscordSRV.debug("Skipping status update cycle, JDA was null");
            }

            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(rate));
            } catch (InterruptedException e) {
                DiscordSRV.error("Broke from Status Updater thread: sleep interrupted");
                return;
            }
        }
    }
}