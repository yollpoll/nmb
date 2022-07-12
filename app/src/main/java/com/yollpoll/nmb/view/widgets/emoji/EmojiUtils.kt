package com.yollpoll.nmb.view.widgets.emoji

import com.yollpoll.nmb.R
import com.yollpoll.nmb.view.widgets.emoji.EmojiUtils
import java.util.ArrayList

/**
 * Created by 鹏祺 on 2017/6/14.
 */
object EmojiUtils {
    var wordEmoji = arrayOf(
        "|∀ﾟ", "(´ﾟДﾟ`)", "(;´Д`)",
        "(｀･ω･)", "(=ﾟωﾟ)=", "| ω・´)",
        "|-` )", "|д` )", "|ー` )",
        "|∀` )", "(つд⊂)", "(ﾟДﾟ≡ﾟДﾟ)",
        "(＾o＾)ﾉ", "(|||ﾟДﾟ)", "( ﾟ∀ﾟ)",
        "( ´∀`)", "(*´∀`)", "(*ﾟ∇ﾟ)",
        "(*ﾟーﾟ)", "(　ﾟ 3ﾟ)", "( ´ー`)",
        "( ・_ゝ・)", "( ´_ゝ`)", "(*´д`)",
        "(・ー・)", "(・∀・)", "(ゝ∀･)",
        "(〃∀〃)", "( ﾟ∀。)", "( `д´)",  // 30
        "(`ε´ )", "(`ヮ´ )", "σ`∀´)",
        " ﾟ∀ﾟ)σ", "ﾟ ∀ﾟ)ノ", "(╬ﾟдﾟ)",
        "(|||ﾟдﾟ)", "( ﾟдﾟ)", "Σ( ﾟдﾟ)",
        "( ;ﾟдﾟ)", "( ;´д`)", "(　д ) ﾟ ﾟ",
        "( ☉д⊙)", "(((　ﾟдﾟ)))", "( ` ・´)",
        "( ´д`)", "( -д-)", "(>д<)",
        "･ﾟ( ﾉд`ﾟ)", "( TдT)", "(￣∇￣)",
        "(￣3￣)", "(￣ｰ￣)", "(￣ . ￣)",
        "(￣皿￣)", "(￣艸￣)", "(￣︿￣)",
        "(￣︶￣)", "ヾ(´ωﾟ｀)", "(*´ω`*)",  // 60
        "(・ω・)", "( ´・ω)", "(｀・ω)",
        "(´・ω・`)", "(`・ω・´)", "( `_っ´)",
        "( `ー´)", "( ´_っ`)", "( ´ρ`)",
        "( ﾟωﾟ)", "(oﾟωﾟo)", "(　^ω^)",
        "(｡◕∀◕｡)", "/( ◕‿‿◕ )\\", "ヾ(´ε`ヾ)",
        "(ノﾟ∀ﾟ)ノ", "(σﾟдﾟ)σ", "(σﾟ∀ﾟ)σ",
        "|дﾟ )", "┃電柱┃", "ﾟ(つд`ﾟ)",
        "ﾟÅﾟ )　", "⊂彡☆))д`)", "⊂彡☆))д´)",
        "⊂彡☆))∀`)", "(´∀((☆ミつ", "（<ゝω・）☆",
        "¯\\_(ツ)_/¯", "☎110", "⚧",  // 90
        "☕", "(`ε´ (つ*⊂)", "\u3000"
    )
    var picLwnEmoji = intArrayOf(
        R.mipmap.lwn_1, R.mipmap.lwn_2, R.mipmap.lwn_3, R.mipmap.lwn_4, R.mipmap.lwn_5,
        R.mipmap.lwn_6, R.mipmap.lwn_7, R.mipmap.lwn_8, R.mipmap.lwn_9, R.mipmap.lwn_10,
        R.mipmap.lwn_11, R.mipmap.lwn_12, R.mipmap.lwn_13, R.mipmap.lwn_14, R.mipmap.lwn_15,
        R.mipmap.lwn_16, R.mipmap.lwn_17, R.mipmap.lwn_18, R.mipmap.lwn_19, R.mipmap.lwn_20,
        R.mipmap.lwn_21, R.mipmap.lwn_22, R.mipmap.lwn_23, R.mipmap.lwn_24, R.mipmap.lwn_25,
        R.mipmap.lwn_26, R.mipmap.lwn_27, R.mipmap.lwn_28, R.mipmap.lwn_29, R.mipmap.lwn_30,
        R.mipmap.lwn_31, R.mipmap.lwn_32, R.mipmap.lwn_33, R.mipmap.lwn_34, R.mipmap.lwn_35,
        R.mipmap.lwn_36, R.mipmap.lwn_37, R.mipmap.lwn_38, R.mipmap.lwn_39, R.mipmap.lwn_40,
        R.mipmap.lwn_41, R.mipmap.lwn_42, R.mipmap.lwn_43, R.mipmap.lwn_44, R.mipmap.lwn_45,
        R.mipmap.lwn_46, R.mipmap.lwn_47, R.mipmap.lwn_48, R.mipmap.lwn_49, R.mipmap.lwn_50,
        R.mipmap.lwn_51, R.mipmap.lwn_52, R.mipmap.lwn_53, R.mipmap.lwn_54, R.mipmap.lwn_55,
        R.mipmap.lwn_56, R.mipmap.lwn_57, R.mipmap.lwn_58, R.mipmap.lwn_59, R.mipmap.lwn_60,
        R.mipmap.lwn_61, R.mipmap.lwn_62, R.mipmap.lwn_63, R.mipmap.lwn_64, R.mipmap.lwn_65,
        R.mipmap.lwn_66, R.mipmap.lwn_67, R.mipmap.lwn_68, R.mipmap.lwn_69, R.mipmap.lwn_70,
        R.mipmap.lwn_71, R.mipmap.lwn_72, R.mipmap.lwn_73, R.mipmap.lwn_74, R.mipmap.lwn_75,
        R.mipmap.lwn_76, R.mipmap.lwn_77, R.mipmap.lwn_78, R.mipmap.lwn_79, R.mipmap.lwn_80,
        R.mipmap.lwn_81, R.mipmap.lwn_82, R.mipmap.lwn_83, R.mipmap.lwn_84, R.mipmap.lwn_85,
        R.mipmap.lwn_86, R.mipmap.lwn_87, R.mipmap.lwn_88, R.mipmap.lwn_89, R.mipmap.lwn_90,
        R.mipmap.lwn_91, R.mipmap.lwn_92, R.mipmap.lwn_93, R.mipmap.lwn_94, R.mipmap.lwn_95,
        R.mipmap.lwn_96, R.mipmap.lwn_97, R.mipmap.lwn_98, R.mipmap.lwn_99, R.mipmap.lwn_100,
        R.mipmap.lwn_101, R.mipmap.lwn_102, R.mipmap.lwn_103, R.mipmap.lwn_104, R.mipmap.lwn_105,
        R.mipmap.lwn_106
    )

    //获取颜文字列表
    val wordEmojiList: List<String>
        get() {
            val emojiList: MutableList<String> = ArrayList()
            for (i in wordEmoji.indices) {
                emojiList.add(wordEmoji[i])
            }
            return emojiList
        }

    //获取芦苇娘表情列表
    val picLwnEmojiList: List<Int>
        get() {
            val emojiList: MutableList<Int> = ArrayList()
            for (i in picLwnEmoji.indices) {
                emojiList.add(picLwnEmoji[i])
            }
            return emojiList
        }
}