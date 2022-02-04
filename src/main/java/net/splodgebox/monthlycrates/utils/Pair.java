package net.splodgebox.monthlycrates.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Pair<A, B> {

    private A key;
    private B value;

}