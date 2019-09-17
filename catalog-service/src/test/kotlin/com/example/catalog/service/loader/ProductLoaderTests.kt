package com.example.catalog.service.loader

import com.example.catalog.loader.ProductLoader
import com.example.catalog.loader.ProductLoader.Companion.descPattern
import com.example.catalog.loader.ProductLoader.Companion.idPattern
import com.example.catalog.loader.ProductLoader.Companion.lineTemplate
import com.example.catalog.loader.ProductLoader.Companion.namePattern
import com.example.catalog.loader.ProductLoader.Companion.pricePattern
import com.example.catalog.loader.ProductLoader.Companion.productTransformer
import com.example.catalog.model.Product
import io.kotlintest.matchers.maps.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FeatureSpec
import io.kotlintest.tables.forAll
import io.kotlintest.tables.headers
import io.kotlintest.tables.row
import io.kotlintest.tables.table
import java.io.ByteArrayInputStream

class ProductLoaderTests : FeatureSpec ({

    feature("Product loading from csv file"){

        scenario("should create map of Products from csv lines. All lines are valid") {
            val csvString = "id;name;price;description\n" +
                    "1;Hammer;10.32;A tool with a heavy metal head\n" +
                    "2;Screwdriver;8.45;A tool with a flattened, cross-shaped, or star-shaped tip\n" +
                    "3;Pliers;17.28;Pincers with parallel, flat, and typically serrated surfaces"

            val (actualProducts, failed) = ProductLoader().loadProducts(ByteArrayInputStream(csvString.toByteArray()))
            val expectedProducts = mapOf("1" to Product("1", "Hammer", 10.32, "A tool with a heavy metal head"),
                    "2" to Product("2", "Screwdriver", 8.45, "A tool with a flattened, cross-shaped, or star-shaped tip"),
                    "3" to Product("3", "Pliers", 17.28, "Pincers with parallel, flat, and typically serrated surfaces"))

            actualProducts shouldContainExactly expectedProducts
            failed shouldBe emptyList()
        }

        scenario("should create map of Products from valid csv lines and list of invalid csv lines. Some lines are invalid") {
            val csvString = "id;name;price;description\n" +
                    "1;Hammer;10.32;A tool with a heavy metal head\n" +
                    "Line is not matched\n" +
                    "3;Pliers;17.28;Pincers with parallel, flat, and typically serrated surfaces\n"+
                    "Another line is not matched"

            val (actualProducts, failed) = ProductLoader().loadProducts(ByteArrayInputStream(csvString.toByteArray()))

            actualProducts shouldContainExactly mapOf("1" to Product("1", "Hammer", 10.32, "A tool with a heavy metal head"),
                    "3" to Product("3", "Pliers", 17.28, "Pincers with parallel, flat, and typically serrated surfaces"))

            failed shouldBe listOf("Line is not matched","Another line is not matched")
        }

        scenario("should create empty map of Product entities and list of invalid csv lines. All lines are invalid") {
            val csvString = "id;name;price;description\n" +
                    "Line is not matched\n" +
                    "Another line is not matched"
            val (actualProducts, failed) = ProductLoader().loadProducts(ByteArrayInputStream(csvString.toByteArray()))

            actualProducts shouldContainExactly emptyMap()

            failed shouldBe listOf("Line is not matched","Another line is not matched")
        }

        scenario("should create empty map of Product entities and empty list of invalid csv lines. Empty csv input with headers") {
            table(
                    headers("line", "result"),
                    row("id;name;price;description",Pair(emptyMap<String, Product>(), emptyList<String>())),
                    row("id;name;price;description\n",Pair(emptyMap(), emptyList())),
                    row("",Pair(emptyMap(), emptyList()))
            ).forAll {
                line, result -> ProductLoader().loadProducts(ByteArrayInputStream(line.toByteArray())) shouldBe result
            }
        }
    }

    feature("Properties in csv line are validated according to regex pattern"){

        scenario("should validate all properties separated by ';' in line") {
            table(
                    headers("props", "valid"),
                    row("1;Hammer;10.32;A tool with a heavy metal head", true),
                    row("1;Hammer;10.32;", true),
                    row("id-1;Product 1;10.10;", true),
                    row("id-1;", false),
                    row("id-1;Product 1;", false),
                    row("plain text", false),
                    row(";;;", false),
                    row(" ", false)
            ).forAll {
                props, valid -> props.matches(lineTemplate) shouldBe valid
            }
        }

        scenario("should validate Id according to Id pattern") {
            table(
                    headers("id", "valid"),
                    row("1234567890_a-z_and_A-Z_only", true),
                    row(" ", false),
                    row("", false),
                    row("\\/()!? and so on", false)
            ).forAll {
                id, valid -> id.matches(Regex(idPattern)) shouldBe valid
            }
        }

        scenario("should validate Name according to Name pattern") {
            table(
                    headers("name", "valid"),
                    row("1234567890 a-z and A-Z only plus _ -/=", true),
                    row("", false),
                    row("\\()!?* and so on are not allowed", false)
            ).forAll {
                name, valid -> name.matches(Regex(namePattern)) shouldBe valid
            }
        }

        scenario("should validate Price according to Price pattern") {
            table(
                    headers("price", "valid"),
                    row("1234567890", true),
                    row("0.", true),
                    row("10.", true),
                    row("0.0", true),
                    row("0.00", true),
                    row("0.0000000", true),
                    row("0,", false),
                    row("0.0.", false),
                    row("0..0", false),
                    row("0..", false),
                    row("-", false),
                    row("-0", false),
                    row("abc", false),
                    row("", false),
                    row("\\()!?* and so on are not allowed", false)
            ).forAll {
                price, valid -> price.matches(Regex(pricePattern)) shouldBe valid
            }
        }

        scenario("should validate Description according to Description pattern") {
            table(
                    headers("name", "valid"),
                    row("1234567890 a-z and A-Z only plus _ -/=", true),
                    row("", true),
                    row("\\()!?* and so on are not allowed", false)
            ).forAll {
                name, valid -> name.matches(Regex(descPattern)) shouldBe valid
            }
        }
    }

    feature("Transforming list of properties into Product entity"){
        scenario("should create Product from valid list of properties"){
            table(
                    headers ("props", "product"),
                    row(listOf("id-1","Product 1", "10.25", "Desc"), Product("id-1","Product 1", 10.25, "Desc"))
            ).forAll{
                props, product -> productTransformer(props) shouldBe product
            }
        }

        scenario("should throw exception if Price property is incorrect"){
            shouldThrow<NumberFormatException> {
                productTransformer(listOf("id-1","Product 1", "Invalid input", "Desc"))
            }
        }
    }

})