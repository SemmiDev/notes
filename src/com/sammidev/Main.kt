package com.sammidev

data class Student(
        var name: String,
        val nim: String
)
data class Siswa(
        val name: String,
        val hobby: List<String>
)

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {

            // Student(name=sammidev, nim=200311)
            listOf(Student("sammidev", "200311")).forEach(::println)

            // 0 -> Student(name=sam, nim=200311)
            // 1 -> Student(name=dev, nim=200312)
            // 2 -> Student(name=sammidev, nim=200313)
            listOf(
                    Student("sam", "200311"),
                    Student("dev", "200312"),
                    Student("sammidev", "200313")
            ).forEachIndexed { index, student -> println("$index -> $student") }

            // Student(name=SAMMIDEV GANTENG, nim=20)
            // Student(name=SAMMIDEV CANTIK, nim=340)
            // Student(name=SAM, nim=120)
            // Student(name=DEV, nim=50)
            listOf(
                    Student("sammidev", "1"),
                    Student("sammidev ganteng", "20"),
                    Student("sammidev cantik", "340"),
                    Student("sam", "120"),
                    Student("dev", "50")
            ).forEach {
                if (it.nim.toInt() > 1) {
                    it.name = it.name.toUpperCase()
                    println(it)
                }
            }

            // Student(name=sam, nim=200311)
            // DATA NOT EQUALS
            // DATA NOT EQUALS
            listOf(
                    Student("sam", "200311"),
                    Student("dev", "200312"),
                    Student("sammidev", "200313")
            ).mapNotNull { value ->
                if (value.name == "sam" && value.nim == "200311")
                    println(value)
                else println("DATA NOT EQUALS")
            }

            // 10=Sammi
            // 20=Dev
            // 30=Aldhy
            val map1 = mutableMapOf(
                    1 to "Sammi",
                    2 to "Dev",
                    3 to "Aldhy"
            ).mapKeys { it.key * 10 }
                    .forEach { println(it) }

            // Sammidev ganteng
            // sam gateng sangat
            // Sam so handsome
            listOf("Sammidev", "sam", "Sam").zip(listOf("ganteng", "gateng sangat", "so handsome")) { item1, item2 ->
                "$item1 $item2"
            }.forEach(::println)


            // ([1, 2, 3], [sam, dev, sammidev])
            val list: List<Pair<Int, String>> = listOf(
                    1 to "sam",
                    2 to "dev",
                    3 to "sammidev"
            )
            val pair: Pair<List<Int>, List<String>> = list.unzip()
            println(pair)

            // Sammidev=8
            // Dev=3
            // Sam=3
            listOf("Sammidev", "Dev", "Sam").associate { Pair(it, it.length) }.forEach(::println)

            // 8 -> Sammidev
            // 3 -> Sam
            // Sammidev -> 8
            listOf("Sammidev", "Dev", "Sam").associateBy { it.length }.forEach { println("${it.key} -> ${it.value}") }

            // Dev -> 3
            // Sam -> 3
            listOf("Sammidev", "Dev", "Sam").associateWith { it.length }.forEach { println("${it.key} -> ${it.value}") }

            // [sammidev1, dev1, sam1, sammidev2, dev2, sam2, sammidev3, dev3, sam3]
            val list1 = listOf(
                    listOf("sammidev1", "dev1", "sam1"),
                    listOf("sammidev2", "dev2", "sam2"),
                    listOf("sammidev3", "dev3", "sam3"),
            )
            val result = list1.flatten()
            println(result)

            // [ngoding1, renang1, ngoding2, renang2, ngoding3, renang3]
            val siswaList = listOf(
                    Siswa("sammidev1", listOf("ngoding1","renang1")),
                    Siswa("sammidev2", listOf("ngoding2","renang2")),
                    Siswa("sammidev3", listOf("ngoding3","renang3")),
            ).flatMap { it.hobby }
            println(siswaList)

            val names = listOf("Sammidev","Dev","Sammi")

            // |Sammidev Dev Sam|
            println(names.joinToString(" ","|","|"))

            // |item Sammidev item Dev item Sam|
            println(names.joinToString(" ","|","|") {
                string -> "item $string"
            })

            // [Sammidev, Sammi]
            // [Dev]
            val (listMatch, listNotMatch) = names.partition { it.length > 3 }
            println(listMatch)
            println(listNotMatch)

            // true
            // false
            // false
            // true
            // false
            println(names.any { it.length > 5 })
            println(names.none { it.length > 5 })
            println(names.all { it.length > 5 })
            println(names.any())
            println(names.none())

            // [samidev, dev, ganteng, ganteng]
            val concat = listOf("samidev","dev") + listOf("ganteng", "ganteng")

            // [dev]
            val substract = listOf("samidev","dev") - "samidev"

            val list11 = listOf("a","b","a","c","d","e")
            // {a=[a, a], b=[b], c=[c], d=[d], e=[e]}
            val re1 = list11.groupBy { it }
            println(re1)
            
            // hal 101



        }
    }
}