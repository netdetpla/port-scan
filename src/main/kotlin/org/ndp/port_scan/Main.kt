package org.ndp.port_scan

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.ndp.port_scan.bean.Host
import org.ndp.port_scan.bean.Port
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.system.exitProcess

object Main {

    private val appStatusDir = File("/tmp/appstatus/")
    private val resultDir = File("/tmp/result/")
    private val resultFile = File("/tmp/result/result")
    private lateinit var ports: String
    private val xPath = XPathFactory.newInstance().newXPath()

    init {
        appStatusDir.mkdirs()
        resultDir.mkdirs()
    }

    private fun parseParam() {
        val param = File("/tmp/conf/busi.conf").readText().split(";")
        val input = File("/input_file")
        input.writeText(param[0].replace(",", "\n"))
        ports = param[1]
        Log.debug("params: ")
        Log.debug(param[0])
        Log.debug(ports)
    }

    private fun execute() {
        Log.info("nmap start")
//        val nmapBuilder = ProcessBuilder("/bin/bash", "-c",
//                "\"nmap -Pn -n -sSV --open -vv -oX /result.xml -p $ports -iL /input_file\""
//        )
        val nmapBuilder = ProcessBuilder("nmap -Pn -n -sSV --open -vv -oX /result.xml -p $ports -iL /input_file".split(" "))
        nmapBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        nmapBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)
        nmapBuilder.directory(File("/"))
        val nmap = nmapBuilder.start()
        nmap.waitFor()
        Log.info("nmap end")
    }

    private fun parseMidResult(): String {
        Log.info("parsing the result of nmap")
        val xml = File("/result.xml")
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml)
        val hostNodes = xPath.evaluate("//host", doc, XPathConstants.NODESET) as NodeList
        val hosts = ArrayList<Host>()
        for (i in 1..hostNodes.length) {
            val addr = (xPath.evaluate("//host[$i]//@addr", doc, XPathConstants.NODE) as Node).textContent
            val portNodes = xPath.evaluate("//host[$i]//port", doc, XPathConstants.NODESET) as NodeList
            val ports = ArrayList<Port>()
            for (j in 1..portNodes.length) {
                val state = (xPath.evaluate(
                        "//host[$i]/ports/port[$j]/state/@state",
                        doc,
                        XPathConstants.NODE
                ) as Node).textContent
                if (state != "open")
                    continue
                val protocol = (xPath.evaluate(
                        "//host[$i]/ports/port[$j]/@protocol",
                        doc,
                        XPathConstants.NODE
                ) as Node).textContent
                val portID = (xPath.evaluate(
                        "//host[$i]/ports/port[$j]/@portid",
                        doc,
                        XPathConstants.NODE
                ) as Node).textContent
                val service = (xPath.evaluate(
                        "//host[$i]/ports/port[$j]/service/@name",
                        doc,
                        XPathConstants.NODE
                ) as Node).textContent
                val product = (xPath.evaluate(
                        "//host[$i]/ports/port[$j]/service/@product",
                        doc,
                        XPathConstants.NODE
                ) as? Node)?.textContent ?: "unknown"
                ports.add(Port(protocol, portID, state, service, product))
            }
            if (ports.size > 0) {
                hosts.add(Host(addr, ports))
            }
        }
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(List::class.java)
        Log.info("finished parsing")
        return adapter.toJson(hosts)
    }

    private fun writeResult(result: String) {
        Log.debug("result: ")
        Log.debug(result)
        Log.info("writing result file")
        resultFile.writeText(result)
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
            // 解析中间文件，写结果
            writeResult(parseMidResult())
        } catch (e: Exception) {
            Log.error(e.toString())
            e.printStackTrace()
            errorEnd(e.toString(), 11)
        }
        // 结束
        successEnd()
        Log.info("port-scan end successfully")
    }
}
