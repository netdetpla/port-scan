package org.ndp.port_scan.bean


class Host(
        val address: String,
        val os: String,
        val hardware: String,
        val ports: List<Port>
)