package com.impact.Construction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;

@AllArgsConstructor
@Getter
public enum Product {
    OAK_LARDER(8, ItemID.OAK_PLANK, 15403, 13566, 2),
    OAK_TABLE(4, ItemID.OAK_PLANK, 15298, 13294, 2),
    // TODO: missing values in MAHOGANY_TABLE
    MAHOGANY_TABLE(6, ItemID.MAHOGANY_PLANK, 15298, 0, 6),
    MYTHICAL_CAPE(3, ItemID.TEAK_PLANK, 15394, 31986, 4);

    private final int requiredPlanks;
    private final int plankId;
    private final int buildSpaceId;
    private final int removeSpaceId;
    private final int index;
}
