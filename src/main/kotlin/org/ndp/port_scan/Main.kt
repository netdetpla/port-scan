package org.ndp.port_scan

import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.lang.Exception
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.system.exitProcess

object Main {

    private val appStatusDir = File("/tmp/appstatus/")
    private val resultDir = File("/tmp/result/")
    private val resultFile = File("/tmp/result/result")
    lateinit var ports: String

    init {
        appStatusDir.mkdirs()
        resultDir.mkdirs()
    }

    private fun parseParam() {
        val param = File("/tmp/conf/busi.conf").readText().split(";")
        val input = File("/input_file")
        input.writeText(param[0].replace(",", "\n"))
        ports = param[1]
    }

    private fun execute() {
        val command = "nmap -Pn -n -sU -sS -oX /result.xml -p $ports -iL /input_file"
        val nmap = Runtime.getRuntime().exec(command)
        nmap.waitFor()
    }

    private fun parseMidResult(): Array<String> {
        val xml = File("/result.xml")
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml)
        val xPath = XPathFactory.newInstance().newXPath()
        val hosts = xPath.evaluate("//host", doc, XPathConstants.NODESET) as NodeList
        return Array(hosts.length) {
            val addr = xPath.evaluate("//@addr", hosts.item(it), XPathConstants.NODE) as Node
            val tcpPorts = xPath.evaluate("//port[@protocol='tcp']", hosts.item(it), XPathConstants.NODESET) as NodeList
            val tcpSet = Array(tcpPorts.length) { tcpIndex ->
                val portID = xPath.evaluate("/@portid", tcpPorts.item(tcpIndex), XPathConstants.NODE) as Node
                portID.textContent
            }

            val udpPorts = xPath.evaluate("//port[@protocol='udp']", hosts.item(it), XPathConstants.NODESET) as NodeList
            val udpSet = Array(udpPorts.length) { udpIndex ->
                val portID = xPath.evaluate("/@portid", udpPorts.item(udpIndex), XPathConstants.NODE) as Node
                portID.textContent
            }
            addr.textContent + "," + tcpSet.joinToString("+") + udpSet.joinToString("+")
        }
    }

    private fun writeResult(result: Array<String>) {
        resultFile.writeText(result.joinToString(";"))
    }

    private fun successEnd() {
        val successFile = File("/tmp/appstatus/0")
        successFile.writeText("")
    }

    private fun errorEnd(message: String, code: Int) {
        val errorFile = File("/tmp/appstatus/1")
        errorFile.writeText(message)
        exitProcess(code)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        Log.info("port-scan start")
        // 获取配置
        parseParam()
        // 执行
        try {
            execute()
            // 解析中间文件
            val result: Array<String> = parseMidResult()
            // 写结果
            writeResult(result)
        } catch (e: Exception) {
            Log.error(e.toString())
            e.printStackTrace()
            errorEnd(e.toString(), 11)
        }
        // 结束
        successEnd()
    }
}
