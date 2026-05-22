package com.example.data.model

data class CalendarEvent(
    val id: String,
    val title: String,
    val description: String,
    val dateString: String, // YYYY-MM-DD
    val timeString: String,
    val category: String, // "Worship", "Sabbath Study", "Community", "Prayer Gathering"
    val location: String = "Main Sanctuary"
)

data class BiblicalHoliday(
    val id: String,
    val name: String,
    val hebrewName: String,
    val scriptureRef: String,
    val dateRange2026: String, // Dates for 2026
    val biblicalDate: String, // e.g. "14th of Nisan"
    val significance: String,
    val practice: String,
    val season: String // "Spring" or "Fall"
)

data class BibleChapter(
    val label: String, // e.g., "Chapter 1" or "Column I"
    val text: String
)

data class BibleBook(
    val id: String,
    val name: String,
    val category: BookCategory,
    val totalChapters: Int,
    val introduction: String,
    val chapters: List<BibleChapter>
)

enum class BookCategory {
    OLD_TESTAMENT,
    NEW_TESTAMENT,
    APOCRYPHA,
    DEAD_SEA_SCROLLS,
    PSEUDEPIGRAPHA
}

enum class CalendarSystem {
    ZADOK,
    HILLEL,
    CONJUNCTION
}

object SeedData {
    val events = listOf(
        CalendarEvent(
            id = "evt1",
            title = "Sabbath Morning Fellowship & Study",
            description = "Join us as we welcome the Sabbath in communal study, followed by a delicious, healthy potluck meal in the courtyard.",
            dateString = "2026-05-23", // Saturday
            timeString = "10:00 AM",
            category = "Sabbath Study",
            location = "Fellowship Hall"
        ),
        CalendarEvent(
            id = "evt2",
            title = "Mid-Week Prayer & Covenant Gathering",
            description = "An evening of intercessory prayer, laying on of hands, and worship focusing on submitted community requests.",
            dateString = "2026-05-27", // Wednesday
            timeString = "07:00 PM",
            category = "Prayer Gathering",
            location = "Sanctuary"
        ),
        CalendarEvent(
            id = "evt3",
            title = "Dead Sea Scrolls Exhibition & Lecture",
            description = "Dr. Rachel Chen presents localized slide projections of the Temple Scroll and Isaiah Scroll, examining their textual transmission.",
            dateString = "2026-05-30", // Saturday
            timeString = "02:00 PM",
            category = "Worship",
            location = "Education Wing"
        ),
        CalendarEvent(
            id = "evt4",
            title = "Sabbath Eve Preparation & Bread Baking",
            description = "Bake whole wheat challah and prepare your hearts together as we prepare for the Friday sunset incoming of Sabbath rest.",
            dateString = "2026-05-29", // Friday
            timeString = "04:30 PM",
            category = "Community",
            location = "Church Kitchen"
        )
    )

    val holidays = listOf(
        BiblicalHoliday(
            id = "h1",
            name = "Passover (Pesach)",
            hebrewName = "פֶּסַח",
            scriptureRef = "Leviticus 23:5, Exodus 12",
            dateRange2026 = "April 2 - April 3, 2026",
            biblicalDate = "14th of Nisan",
            significance = "Commemorates the redemption of Israel from Egyptian bondage and the passing over of the plague of the firstborn through the blood of the lamb.",
            practice = "Seder meal, eating matzah (unleavened bread), telling the Exodus story, and drinking the cups of redemption.",
            season = "Spring"
        ),
        BiblicalHoliday(
            id = "h2",
            name = "Feast of Unleavened Bread (Chag HaMatzot)",
            hebrewName = "חַג הַמַּצּוֹת",
            scriptureRef = "Leviticus 23:6-8, Exodus 12:15",
            dateRange2026 = "April 3 - April 9, 2026",
            biblicalDate = "15th - 21st of Nisan",
            significance = "Represents purging the old leaven of sin and malice, walking in sincerity, truth, and spiritual consecration.",
            practice = "Eating unleavened bread for seven straight days, avoiding any leavened food products (Chametz), holy convocations.",
            season = "Spring"
        ),
        BiblicalHoliday(
            id = "h3",
            name = "Feast of Weeks / Pentecost (Shavuot)",
            hebrewName = "שָׁבוּעוֹת",
            scriptureRef = "Leviticus 23:15-21, Acts 2:1",
            dateRange2026 = "May 22 - May 24, 2026",
            biblicalDate = "6th of Sivan",
            significance = "Celebrates the giving of the Torah at Mount Sinai and the subsequent outpouring of the Holy Spirit on the disciples in Jerusalem.",
            practice = "Staying up all night studying scripture, decorating the sanctuary with fresh green branches, reading the Book of Ruth.",
            season = "Spring"
        ),
        BiblicalHoliday(
            id = "h4",
            name = "Feast of Trumpets (Yom Teruah)",
            hebrewName = "יוֹם תְּרוּעָה",
            scriptureRef = "Leviticus 23:23-25, Numbers 29:1",
            dateRange2026 = "September 11 - September 12, 2026",
            biblicalDate = "1st of Tishrei",
            significance = "The day of blowing the shofars, calling the soul to wakefulness, self-examination, and announcing the supreme Kingship of God.",
            practice = "Hearing the sounding of the Shofar, dramatic candle lighting, eating apples dipped in raw honey for a sweet biblical year.",
            season = "Fall"
        ),
        BiblicalHoliday(
            id = "h5",
            name = "Day of Atonement (Yom Kippur)",
            hebrewName = "יוֹם כִּפּוּר",
            scriptureRef = "Leviticus 23:26-32, Hebrews 9",
            dateRange2026 = "September 20 - September 21, 2026",
            biblicalDate = "10th of Tishrei",
            significance = "The holiest day of the year, representing cleansing, reconciliation, forgiveness, and absolute redemption through mercy.",
            practice = "A complete 25-hour fast, abstaining from food, drink, of any luxury. Dedicated intensive prayer and confession (Viduy).",
            season = "Fall"
        ),
        BiblicalHoliday(
            id = "h6",
            name = "Feast of Tabernacles (Sukkot)",
            hebrewName = "סֻכּוֹת",
            scriptureRef = "Leviticus 23:33-43, John 7:37",
            dateRange2026 = "September 25 - October 2, 2026",
            biblicalDate = "15th - 22nd of Tishrei",
            significance = "Reminds us of God's physical protection and presence in the wilderness. Anticipates the ultimate Messianic Age and rest.",
            practice = "Building temporary outdoor booths (Sukkot), dwelling inside them, waving the four species (Lulav & Etrog), supreme rejoicing.",
            season = "Fall"
        )
    )

    val bibleBooks = listOf(
        BibleBook(
            id = "genesis",
            name = "Genesis (Bereshit)",
            category = BookCategory.OLD_TESTAMENT,
            totalChapters = 50,
            introduction = "The book of beginnings. Detailing creation, the covenant with Abraham, Isaac, Jacob, and the ultimate migration of Israel to Egypt.",
            chapters = listOf(
                BibleChapter(
                    label = "Chapter 1",
                    text = "1 In the beginning God created the heaven and the earth. 2 And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters.\n\n3 And God said, Let there be light: and there was light. 4 And God saw the light, that it was good: and God divided the light from the darkness. 5 And God called the light Day, and the darkness he called Night. And the evening and the morning were the first day.\n\n6 And God said, Let there be a firmament in the midst of the waters, and let it divide the waters from the waters.\n\n... 27 So God created man in his own image, in the image of God created he him; male and female created he them. 28 And God blessed them, and God said unto them, Be fruitful, and multiply, and replenish the earth..."
                ),
                BibleChapter(
                    label = "Chapter 2",
                    text = "1 Thus the heavens and the earth were finished, and all the host of them. 2 And on the seventh day God ended his work which he had made; and he rested on the seventh day from all his work which he had made.\n\n3 And God blessed the seventh day, and sanctified it: because that in it he had rested from all his work which God created and made.\n\n4 These are the generations of the heavens and of the earth when they were created..."
                )
            )
        ),
        BibleBook(
            id = "psalms",
            name = "Psalms (Tehillim)",
            category = BookCategory.OLD_TESTAMENT,
            totalChapters = 150,
            introduction = "The prayer book and hymnal of ancient Israel, spanning absolute despair to ecstatic worship.",
            chapters = listOf(
                BibleChapter(
                    label = "Psalm 23",
                    text = "1 The LORD is my shepherd; I shall not want. 2 He maketh me to lie down in green pastures: he leadeth me besides the still waters.\n\n3 He restoreth my soul: he leadeth me in the paths of righteousness for his name's sake.\n\n4 Yea, though I walk through the valley of the shadow of death, I will fear no evil: for thou art with me; thy rod and thy staff they comfort me.\n\n5 Thou preparest a table before me in the presence of mine enemies: thou anointest my head with oil; my cup runneth over. 6 Surely goodness and mercy shall follow me all the days of my life: and I will dwell in the house of the LORD for ever."
                ),
                BibleChapter(
                    label = "Psalm 121",
                    text = "1 I will lift up mine eyes unto the hills, from whence cometh my help. 2 My help cometh from the LORD, which made heaven and earth.\n\n3 He will not suffer thy foot to be moved: he that keepeth thee will not slumber. 4 Behold, he that keepeth Israel shall neither slumber nor sleep.\n\n5 The LORD is thy keeper: the LORD is thy shade upon thy right hand. 6 The sun shall not smite thee by day, nor the moon by night. 7 The LORD shall preserve thee from all evil: he shall preserve thy soul. 8 The LORD shall preserve thy going out and thy coming in from this time forth, and even for evermore."
                )
            )
        ),
        BibleBook(
            id = "john",
            name = "John (Leviticus of the NT)",
            category = BookCategory.NEW_TESTAMENT,
            totalChapters = 21,
            introduction = "The mystical Gospel detailing Yeshua the Messiah as the eternal Logos who tabernacled among humanity.",
            chapters = listOf(
                BibleChapter(
                    label = "Chapter 1",
                    text = "1 In the beginning was the Word, and the Word was with God, and the Word was God. 2 The same was in the beginning with God.\n\n3 All things were made by him; and without him was not any thing made that was made. 4 In him was life; and the life was the light of men. 5 And the light shineth in darkness; and the darkness comprehended it not.\n\n... 14 And the Word was made flesh, and dwelt among us, (and we beheld his glory, the glory as of the only begotten of the Father,) full of grace and truth."
                )
            )
        ),
        BibleBook(
            id = "tobit",
            name = "Tobit",
            category = BookCategory.APOCRYPHA,
            totalChapters = 14,
            introduction = "An apocryphal narrative set in Nineveh, detailing the faithfulness of the blind Tobit, the journey of his son Tobias with the archangel Raphael, and deliverance from a demonic curse.",
            chapters = listOf(
                BibleChapter(
                    label = "Chapter 1",
                    text = "1 This book tells the story of Tobit, a man of the tribe of Naphtali, who in the days of Shalmaneser, king of the Assyrians, was taken captive from Thisbe...\n\n3 I, Tobit, walked in pathways of truth and righteousness all the days of my life, and I performed many acts of charity for my kindred and my nation who had gone with me into captivity to Nineveh in the land of the Assyrians.\n\n16 In the days of Shalmaneser I performed many acts of charity for my kindred. I would give my bread to the hungry and my clothing to the naked; and if I saw the dead body of any of my people thrown out behind the wall of Nineveh, I would bury it."
                )
            )
        ),
        BibleBook(
            id = "wisdom",
            name = "Wisdom of Solomon",
            category = BookCategory.APOCRYPHA,
            totalChapters = 19,
            introduction = "A profound Hellenistic theological defense of Israel's monotheism and wisdom against pagan philosophy.",
            chapters = listOf(
                BibleChapter(
                    label = "Chapter 1",
                    text = "1 Love righteousness, you rulers of the earth; think of the Lord with uprightness, and seek him with sincerity of heart; 2 because he is found by those who do not put him to the test, and manifests himself to those who do not distrust him.\n\n3 For perverse thoughts separate people from God, and when his power is tested, it exposes the foolish; 4 because wisdom will not enter a deceitful soul, or dwell in a body enslaved to sin."
                )
            )
        ),
        BibleBook(
            id = "enoch",
            name = "1 Enoch (Book of Henok)",
            category = BookCategory.PSEUDEPIGRAPHA,
            totalChapters = 108,
            introduction = "An ancient apocalyptic work attributed to the seven from Adam, detailing heavenly secrets, the fall of the Watchers (Genesis 6), the Nephilim, and the restoration of all things.",
            chapters = listOf(
                BibleChapter(
                    label = "Chapter 1",
                    text = "1 The words of the blessing of Enoch, wherewith he blessed the elect and righteous, who will be living in the day of tribulation, when all the wicked and godless are to be removed.\n\n2 And he took up his parable and said - Enoch a righteous man, whose eyes were opened by God, saw the vision of the Holy One in the heavens, which the angels showed me, and from them I heard everything, and from them I understood as I saw, but not for this generation, but for a remote one which is for to come."
                )
            )
        ),
        BibleBook(
            id = "1qisa",
            name = "The Great Isaiah Scroll [1QIsa]",
            category = BookCategory.DEAD_SEA_SCROLLS,
            totalChapters = 1,
            introduction = "Found in Cave 1 at Qumran, this is the most complete and oldest biblical manuscript of Isaiah in existence, dating to 125 BCE. Displays marvelous textual consistency with modern Bibles, with subtle spelling/accidental variations.",
            chapters = listOf(
                BibleChapter(
                    label = "Excerpts: Isaiah 53 (Messianic)",
                    text = "Direct translation comparison from Damascus Cave 1 Hebrew:\n\n1 Who has believed our report? And to whom has the arm of YHWH been revealed? 2 For he grew up before him as a tender plant, and as a root out of dry ground. He had no form nor majesty that we should look upon him, nor appearance that we should desire him.\n\n3 He was despised and avoided by men, a man of pains, and acquainted with disease; and as one from whom people hide their faces he was despised, and we esteemed him not.\n\n[Scribe Correction Note in Scroll: Line 6 has an inline correction of \"He\" instead of \"We\", aligning precisely with early textual transmissions. The margin contains the paleo-Hebrew naming of Yahveh, showing the extreme sanctity given to the Tetragrammaton by Qumran scribes.]"
                )
            )
        ),
        BibleBook(
            id = "1qs",
            name = "The Community Rule [1QS]",
            category = BookCategory.DEAD_SEA_SCROLLS,
            totalChapters = 1,
            introduction = "Also known as the Manuel of Discipline. It outlines the radical rules of the Yahad (The Community group), their initiation covenants, common ownership of tools, and rigorous physical hygiene rules.",
            chapters = listOf(
                BibleChapter(
                    label = "Column I - Covenant Entry",
                    text = "1 For the Instructor, that he may guide the holy ones in the way of righteousness: To seek God with all the heart and soul, and to do what is good and upright before Him, as He commanded by the hand of Moses and His servants the Prophets.\n\n5 And to love all that He has chosen, and to hate all that He has rejected. To keep far from all evil and to cling to all good deeds. To practice truth, righteousness, and justice upon the earth, and to walk no longer in the stubbornness of a guilty heart...\n\n[Initiation Rule: All who enter the Covenant of Community shall pledge their personal property, food, and clothing to the common fund monitored by the Overseer of Rabim.]"
                )
            )
        ),
        BibleBook(
            id = "11q19",
            name = "The Temple Scroll [11Q19]",
            category = BookCategory.DEAD_SEA_SCROLLS,
            totalChapters = 1,
            introduction = "The longest of the Dead Sea Scrolls (nearly 27 feet), written in the first person as if spoken directly by God to Moses, describing the idealized architecture of the Temple and its sacrificial calendar.",
            chapters = listOf(
                BibleChapter(
                    label = "Column XXX - The Outer Sanctuary",
                    text = "1 You shall build a wall around the inner court, 100 cubits wide, made of dressed stone. Its gates shall open to the east, south, and west...\n\n12 The priests shall occupy offices nearby. No unclean person, nor anyone with physical blemish, shall set foot within My courts. For I, Jehovah, dwell in the midst of My children forever.\n\n[Festivals of Oil and Wine: This scroll uniquely outlines extra annual agricultural festival times: the Festival of New Wine (60 days after Shavuot) and the Festival of New Oil (another 60 days later).]"
                )
            )
        ),
        BibleBook(
            id = "jubilees",
            name = "The Book of Jubilees",
            category = BookCategory.PSEUDEPIGRAPHA,
            totalChapters = 50,
            introduction = "Also known as Lesser Genesis, this ancient work claims to be revelation given to Moses on Mount Sinai, outlining the history of the world in 49-year 'Jubilee' cycles.",
            chapters = listOf(
                BibleChapter(
                    label = "Chapter 1",
                    text = "1 And it came to pass in the first year of the exodus of the children of Israel out of Egypt, in the third month, on the sixteenth day of the month, that the Lord spake unto Moses, saying: 'Come up to Me on the Mount, and I will give thee the two tables of stone of the law and of the commandment, which I have written, that thou mayest teach them.'\n\n2 And Moses went up into the mount of God, and the glory of the Lord abode upon Mount Sinai, and a cloud overshadowed it six days."
                )
            )
        ),
        BibleBook(
            id = "testament_levi",
            name = "The Testament of Levi",
            category = BookCategory.PSEUDEPIGRAPHA,
            totalChapters = 19,
            introduction = "Part of the Testaments of the Twelve Patriarchs, Levi recounts his heavenly visions, his consecration as priest of God, and admonitions of purity and wisdom for his descendants.",
            chapters = listOf(
                BibleChapter(
                    label = "Chapter 1 - The Vision",
                    text = "1 The copy of the words of Levi, the things which he ordained unto his sons, according to all that they should do, and what should befall them until the day of judgment.\n\n2 He was in health when he called them to him, but it had been revealed to him that he should die. And when they were gathered together he said to them: 'I, Levi, was born in Haran, and I came with my father to Shechem... I prayed to the Lord that I might be saved, and there fell upon me a sleep, and I beheld a high mountain, and behold, the heavens were opened, and an angel of God said to me: Levi, enter!'"
                )
            )
        )
    )
}
