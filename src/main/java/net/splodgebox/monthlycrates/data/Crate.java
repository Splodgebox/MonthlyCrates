package net.splodgebox.monthlycrates.data;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.splodgebox.monthlycrates.utils.ItemStackBuilder;
import net.splodgebox.monthlycrates.utils.RandomCollection;
import net.splodgebox.monthlycrates.utils.XMaterial;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Crate {

    private final String id;
    private final String name;
    private final List<String> description;
    private final XMaterial material;

    private final List<XMaterial> colors;
    private final int shuffleTime;
    private final boolean duplicateReward;

    private final RandomCollection<Reward> bonusRewards;
    private final RandomCollection<Reward> rewards;

    public ItemStack create(String player) {
        return new ItemStackBuilder(material.parseItem())
                .setName(name)
                .setLore(description)
                .nbt()
                .set("MonthlyCrates", id)
                .set("SuckMeDaddy<3", UUID.randomUUID().toString())
                .build(ImmutableMap.of("%player%", player));
    }

}
