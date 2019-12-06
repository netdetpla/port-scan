package org.ndp.port_scan.bean

import org.ndp.port_scan.bean.Port

class Host(
        val address: String,
        val ports: List<Port>
)