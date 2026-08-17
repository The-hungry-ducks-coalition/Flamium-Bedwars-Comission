/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2021 Andrei Dascălu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: andrew.dascalu@gmail.com
 */

package com.andrei1058.bedwars.cosmetics;

public class CosmeticData {

    private final String id;
    private final String type;
    private final String displayName;
    private final double price;
    private final String currency;
    private final String particles;
    private final int duration;

    public CosmeticData(String id, String type, String displayName, double price, String currency, String particles, int duration) {
        this.id = id;
        this.type = type;
        this.displayName = displayName;
        this.price = price;
        this.currency = currency;
        this.particles = particles;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public String getParticles() {
        return particles;
    }

    public int getDuration() {
        return duration;
    }
}
