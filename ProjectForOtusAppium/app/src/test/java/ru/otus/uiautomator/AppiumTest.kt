package ru.otus.uiautomator

import io.appium.java_client.AppiumDriver
import io.appium.java_client.MobileBy
import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.pagefactory.AppiumFieldDecorator
import io.appium.java_client.remote.MobileCapabilityType
import io.qameta.allure.Allure
import io.qameta.allure.Severity
import io.qameta.allure.Description
import io.qameta.allure.SeverityLevel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.net.URL

class AppiumTest {

    //драйвер для взаимодействия с сервером Appium (и эмулятором Android через него)
    var driver: AppiumDriver<MobileElement>? = null

    @Before
    fun setup() {
        val capabilities = DesiredCapabilities()
        //подключаемся к серверу
        val serverAddress = URL("http://127.0.0.1:4723")
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android")

        // Указываем уже установленное приложение
        capabilities.setCapability("appPackage", "com.gpn.azs.kz")
        capabilities.setCapability("appActivity", "com.gpn.azs.main.MainActivity")

        // Чтобы Appium не переустанавливал и не сбрасывал данные
        capabilities.setCapability("noReset", true)
        capabilities.setCapability("fullReset", false)

        // Создаем драйвер
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2")
        //создаем драйвер для Android
        driver = AndroidDriver(serverAddress, capabilities)
    }

    @After
    fun tearDown() {
        //при завершении теста останавливаем приложение
        driver?.quit()
    }

    @Test
    @Description("Ошибка авторизации")
    @Severity(SeverityLevel.BLOCKER)
    fun `Error avtorization`() {
        val page = MainPage(driver!!)
        page.inputForNumber?.click()
        page.inputForNumber?.sendKeys("9999999999")
        driver!!.hideKeyboard()
        assertEquals("9999999999", page.inputForNumber?.text)
        page.applyNumber?.click()
        page.applyNumber?.click()
        page.applyNumber?.click()
        page.applyNumber?.click()
        assertEquals("Попробуйте позже", page.errorText?.text)
    }

    @Test
    @Description("Экран службы поддержки")
    @Severity(SeverityLevel.MINOR)
    fun `Support screen`() {
        val page = MainPage(driver!!)

       step("Переход на экран Служба поддержки") {
            page.supportButton?.click()
        }

        step("Переход на правила участия") {
            page.termsConditions?.click()
        }

        step("Проверка PDF") {
            val wait = WebDriverWait(driver, 10)
            val pdfList = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("com.gpn.azs.kz:id/pdf_pages"))
            )
            assertNotNull(pdfList)
        }

        step("Возврат на экрна службы поддержки") {
            page.backButton?.click()
            page.backButton?.click() //да, два раза, да мы в курсе, да мы разрабы уже правят ^^
        }

        step("Проверка версии приложения") {
            val appVersion = page.appVersion?.text.toString()
            assertTrue(appVersion.contains("3.0.0"))
        }

        step("Возврат на экрна личного кабинета") {
            page.backButton?.click()
        }
    }

    @Test
    @Description("Экран акций")
    @Severity(SeverityLevel.NORMAL)
    fun `Scroll to promo and verify content`() {
        val page = MainPage(driver!!)
        page.listOfBanners?.click()
        val expectedTitle = "Акция «Пятый кофе в подарок»"
        val expectedPeriod = "с 02.10.2023 по 31.12.2025"
        scrollRecyclerNTimes(2)
        val periodText = page.periodText?.getAttribute("text")
        val titleText = page.titleText?.getAttribute("text")
        assertEquals(expectedTitle, titleText)
        assertEquals(expectedPeriod, periodText)
    }

    private fun scrollRecyclerNTimes(times: Int) {
        val scrollCmd = """
        new UiScrollable(
            new UiSelector().className("androidx.recyclerview.widget.RecyclerView")
        )
    """.trimIndent()

        repeat(times) { _: Int ->
            driver!!.findElement(MobileBy.AndroidUIAutomator("$scrollCmd.scrollForward()"))
            Thread.sleep(300L)
        }
    }
}

abstract class Page(d: AppiumDriver<*>) {
    init {
        PageFactory.initElements(AppiumFieldDecorator(d), this)
    }
}

fun <T> step(stepName: String, testBody: () -> T) = Allure.step(stepName, testBody)

class MainPage(driver: AppiumDriver<*>) : Page(driver) {

    @FindBy(id = "com.gpn.azs.kz:id/support")
    val supportButton: MobileElement? = null

    @FindBy(id = "com.gpn.azs.kz:id/input")
    val inputForNumber: MobileElement? = null

    @FindBy(id = "com.gpn.azs.kz:id/apply")
    val applyNumber: MobileElement? = null

    @FindBy(id = "com.gpn.azs.kz:id/error_text")
    val errorText: MobileElement? = null

    @FindBy(id = "com.gpn.azs.kz:id/app_version")
    val appVersion: MobileElement? = null

    @FindBy(id = "com.gpn.azs.kz:id/terms_conditions")
    val termsConditions: MobileElement? = null

    @FindBy(xpath = "//android.widget.ImageButton")
    val backButton: MobileElement? = null

    @FindBy(id = "com.gpn.azs.kz:id/btn_contact_operator")
    val contactOperator: MobileElement? = null

    @FindBy(id = "com.gpn.azs.kz:id/main_action_button")
    val listOfBanners: MobileElement? = null

    @FindBy(xpath = "//android.widget.TextView[@resource-id=\"com.gpn.azs.kz:id/period\" and @text=\"с 02.10.2023 по 31.12.2025\"]")
    val periodText: MobileElement? = null

    @FindBy(xpath = "//android.widget.TextView[@resource-id=\"com.gpn.azs.kz:id/title\" and @text=\"Акция «Пятый кофе в подарок»\"]\n")
    val titleText: MobileElement? = null

}
