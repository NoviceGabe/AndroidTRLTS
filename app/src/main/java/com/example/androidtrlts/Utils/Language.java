package com.example.androidtrlts.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import kotlin.jvm.internal.Intrinsics;

public final class Language implements Comparable {
    @NotNull
    private final String code;

    @NotNull
    public final String getDisplayName() {
        String var10000 = (new Locale(this.code)).getDisplayName();
        Intrinsics.checkExpressionValueIsNotNull(var10000, "Locale(code).displayName");
        return var10000;
    }

    public boolean equals(@Nullable Object other) {
        if (other == (Language)this) {
            return true;
        } else if (!(other instanceof Language)) {
            return false;
        } else {
            Language otherLang = (Language)other;
            return Intrinsics.areEqual(otherLang.code, this.code);
        }
    }

    @NotNull
    public String toString() {
        return this.getDisplayName();
    }

    public int compareTo(@NotNull Language other) {
        Intrinsics.checkParameterIsNotNull(other, "other");
        return this.getDisplayName().compareTo(other.getDisplayName());
    }

    // $FF: synthetic method
    // $FF: bridge method
    public int compareTo(Object var1) {
        return this.compareTo((Language)var1);
    }

    public int hashCode() {
        return this.code.hashCode();
    }

    @NotNull
    public final String getCode() {
        return this.code;
    }

    public Language(@NotNull String code) {
        super();
        Intrinsics.checkParameterIsNotNull(code, "code");
        this.code = code;
    }
}

