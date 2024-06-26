! function(e, t) {
    "object" == typeof exports && "object" == typeof module ? module.exports = t(require("ExcelJS")) : "function" == typeof define && define.amd ? define(["ExcelJS"], t) : "object" == typeof exports ? exports.Table2Excel = t(require("ExcelJS")) : e.Table2Excel = t(e.ExcelJS)
}(window, function(e) {
    return function(e) {
        var t = {};

        function n(r) {
            if (t[r]) return t[r].exports;
            var o = t[r] = {
                i: r,
                l: !1,
                exports: {}
            };
            return e[r].call(o.exports, o, o.exports, n), o.l = !0, o.exports
        }
        return n.m = e, n.c = t, n.d = function(e, t, r) {
            n.o(e, t) || Object.defineProperty(e, t, {
                enumerable: !0,
                get: r
            })
        }, n.r = function(e) {
            "undefined" != typeof Symbol && Symbol.toStringTag && Object.defineProperty(e, Symbol.toStringTag, {
                value: "Module"
            }), Object.defineProperty(e, "__esModule", {
                value: !0
            })
        }, n.t = function(e, t) {
            if (1 & t && (e = n(e)), 8 & t) return e;
            if (4 & t && "object" == typeof e && e && e.__esModule) return e;
            var r = Object.create(null);
            if (n.r(r), Object.defineProperty(r, "default", {
                    enumerable: !0,
                    value: e
                }), 2 & t && "string" != typeof e)
                for (var o in e) n.d(r, o, function(t) {
                    return e[t]
                }.bind(null, o));
            return r
        }, n.n = function(e) {
            var t = e && e.__esModule ? function() {
                return e.default
            } : function() {
                return e
            };
            return n.d(t, "a", t), t
        }, n.o = function(e, t) {
            return Object.prototype.hasOwnProperty.call(e, t)
        }, n.p = "", n(n.s = 3)
    }([function(e, t, n) {
        "use strict";
        Object.defineProperty(t, "__esModule", {
            value: !0
        }), t.argb = t.mergeCells = t.cellPosition = t.columnIndex = t.saveAsExcel = void 0;
        var r = n(1),
            o = n(6),
            l = (t.saveAsExcel = function(e) {
                var t = arguments.length > 1 && void 0 !== arguments[1] ? arguments[1] : "table",
                    n = arguments.length > 2 && void 0 !== arguments[2] ? arguments[2] : "xlsx",
                    l = r.MIME_TYPES[n];
                l ? e.xlsx.writeBuffer().then(function(e) {
                    (0, o.saveAs)(new Blob([e.buffer], {
                        type: l
                    }), t + "." + n)
                }) : console.error(n + " file extension is not supported")
            }, function(e) {
                var t = "A".charCodeAt(0);
                return String.fromCharCode(t + e - 1)
            }),
            i = t.columnIndex = function(e) {
                var t = void 0;
                if ((e += 1) <= 26) t = l(e);
                else {
                    var n = e % 26,
                        r = Math.floor(e / 26);
                    t = 0 === n ? l(r - 1) + l(26) : l(r) + l(n)
                }
                return t
            },
            a = t.cellPosition = function(e, t) {
                return "" + i(e) + (t + 1)
            };
        t.mergeCells = function(e, t, n, r, o) {
            var l = a(t, n),
                i = a(r, o);
            return e.mergeCells(l, i), e.getCell(l)
        }, t.argb = function(e) {
            var t = e.split("(")[1].split(")")[0].split(",").map(function(e, t) {
                return 3 === t ? 255 * e : e
            });
            return 3 === t.length && t.push(255), t.unshift(t.pop()), t.map(function(e) {
                var t = parseInt(e).toString(16);
                return 1 === t.length ? "0" + t : t
            }).join("").toUpperCase()
        }
    }, function(e, t, n) {
        "use strict";
        Object.defineProperty(t, "__esModule", {
            value: !0
        });
        t.MIME_TYPES = {
            xls: "application/vnd.ms-excel",
            xlsx: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }, t.WIDTH_RATIO = .14
    }, function(e, t, n) {
        "use strict";
        Object.defineProperty(t, "__esModule", {
            value: !0
        });
        var r = c(n(9)),
            o = c(n(10)),
            l = c(n(11)),
            i = c(n(12)),
            a = c(n(13)),
            u = c(n(14));

        function c(e) {
            return e && e.__esModule ? e : {
                default: e
            }
        }
        t.default = {
            fontPlugin: r.default,
            fillPlugin: o.default,
            formPlugin: l.default,
            alignmentPlugin: i.default,
            hyperlinkPlugin: a.default,
            autoWidthPlugin: u.default
        }
    }, function(e, t, n) {
        "use strict";
        Object.defineProperty(t, "__esModule", {
            value: !0
        });
        var r = i(n(4)),
            o = i(n(2)),
            l = function(e) {
                if (e && e.__esModule) return e;
                var t = {};
                if (null != e)
                    for (var n in e) Object.prototype.hasOwnProperty.call(e, n) && (t[n] = e[n]);
                return t.default = e, t
            }(n(0));

        function i(e) {
            return e && e.__esModule ? e : {
                default: e
            }
        }
        r.default.plugins = o.default, r.default.utils = l, t.default = r.default
    }, function(e, t, n) {
        "use strict";
        Object.defineProperty(t, "__esModule", {
            value: !0
        });
        var r = function() {
                function e(e, t) {
                    for (var n = 0; n < t.length; n++) {
                        var r = t[n];
                        r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), Object.defineProperty(e, r.key, r)
                    }
                }
                return function(t, n, r) {
                    return n && e(t.prototype, n), r && e(t, r), t
                }
            }(),
            o = u(n(5)),
            l = n(0),
            i = n(1),
            a = u(n(2));

        function u(e) {
            return e && e.__esModule ? e : {
                default: e
            }
        }
        var c = ["workbookCreated", "worksheetCreated", "worksheetCompleted", "workcellCreated"],
            f = {
                workbook: {
                    views: [{
                        x: 0,
                        y: 0,
                        width: 1e4,
                        height: 2e4,
                        firstSheet: 0,
                        activeTab: 1,
                        visibility: "visible"
                    }]
                },
                widthRatio: i.WIDTH_RATIO,
                plugins: [].concat(function(e) {
                    if (Array.isArray(e)) {
                        for (var t = 0, n = Array(e.length); t < e.length; t++) n[t] = e[t];
                        return n
                    }
                    return Array.from(e)
                }(Object.values(a.default)))
            },
            s = function() {
                function e() {
                    var t = this,
                        n = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : "table",
                        r = arguments.length > 1 && void 0 !== arguments[1] ? arguments[1] : {};
                    ! function(e, t) {
                        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function")
                    }(this, e), this.tables = Array.from("string" == typeof n ? document.querySelectorAll(n) : n), this.options = Object.assign({}, f, r), this.plugins = {}, c.forEach(function(e) {
                        t.plugins[e] = t.options.plugins.filter(function(t) {
                            return t[e]
                        }).map(function(t) {
                            return t[e]
                        })
                    }), this.pluginContext = {}
                }
                return r(e, [{
                    key: "_invokePlugin",
                    value: function(e) {
                        var t = this,
                            n = arguments.length > 1 && void 0 !== arguments[1] ? arguments[1] : {};
                        this.pluginContext = Object.assign({}, this.pluginContext, n), this.plugins[e].forEach(function(e) {
                            return e.call(t, t.pluginContext)
                        })
                    }
                }, {
                    key: "toExcel",
                    value: function() {
                        var e = this,
                            t = this.tables,
                            n = this.options,
                            r = new o.default.Workbook;
                        return Object.assign(r, n), this._invokePlugin("workbookCreated", {
                            workbook: r,
                            tables: t
                        }), t.forEach(function(t, n) {
                            var o = r.addWorksheet("Sheet " + (n + 1));
                            e._invokePlugin("worksheetCreated", {
                                worksheet: o,
                                table: t
                            }), e.toSheet(t, o), e._invokePlugin("worksheetCompleted", {
                                worksheet: o,
                                table: t
                            })
                        }), this.workbook = r
                    }
                }, {
                    key: "toSheet",
                    value: function(e, t) {
                        var n = this,
                            r = e.rows.length,
                            o = 0;
                        if (e.rows.length > 0)
                            for (var i = 0; i < e.rows[0].cells.length; i++) o += e.rows[0].cells[i].colSpan;
                        var a = [];
                        Array.from(e.rows).forEach(function(e) {
                            Array.from(e.cells).forEach(function(e) {
                                a.push({
                                    rowRange: {},
                                    colRange: {},
                                    el: e
                                })
                            })
                        });
                        for (var u = [], c = 0; c < r; c++) {
                            for (var f = [], s = 0; s < o; s++) f.push({
                                cell: null
                            });
                            u.push(f)
                        }
                        for (var d = 0, p = 0; p < r; p++)
                            for (var v = 0; v < o; v++)
                                if (!u[p][v].cell) {
                                    var h = a[d++],
                                        g = h.el,
                                        b = g.rowSpan,
                                        w = g.colSpan;
                                    h.rowRange = {
                                        from: p,
                                        to: p
                                    }, h.colRange = {
                                        from: v,
                                        to: v
                                    };
                                    for (var y = p; y < p + b; y++)
                                        for (var m = v; m < v + w; m++) u[y][m].cell = h, h.colRange.to = m, h.rowRange.to = y
                                } a.forEach(function(e) {
                            var r = e.rowRange,
                                o = e.colRange,
                                i = e.el,
                                a = i.innerText,
                                u = (0, l.mergeCells)(t, o.from, r.from, o.to, r.to),
                                c = getComputedStyle(i);
                            u.value = a, n._invokePlugin("workcellCreated", {
                                workcell: u,
                                cell: i,
                                rowRange: r,
                                colRange: o,
                                cellStyle: c
                            })
                        })
                    }
                }, {
                    key: "export",
                    value: function(e, t) {
                        this.workbook || this.toExcel(), (0, l.saveAsExcel)(this.workbook, e, t)
                    }
                }]), e
            }();
        t.default = s
    }, function(t, n) {
        t.exports = e
    }, function(e, t, n) {
        var r, o = o || function(e) {
            "use strict";
            if (!(void 0 === e || "undefined" != typeof navigator && /MSIE [1-9]\./.test(navigator.userAgent))) {
                var t = function() {
                        return e.URL || e.webkitURL || e
                    },
                    n = e.document.createElementNS("http://www.w3.org/1999/xhtml", "a"),
                    r = "download" in n,
                    o = /constructor/i.test(e.HTMLElement) || e.safari,
                    l = /CriOS\/[\d]+/.test(navigator.userAgent),
                    i = function(t) {
                        (e.setImmediate || e.setTimeout)(function() {
                            throw t
                        }, 0)
                    },
                    a = function(e) {
                        setTimeout(function() {
                            "string" == typeof e ? t().revokeObjectURL(e) : e.remove()
                        }, 4e4)
                    },
                    u = function(e) {
                        return /^\s*(?:text\/\S*|application\/xml|\S*\/\S*\+xml)\s*;.*charset\s*=\s*utf-8/i.test(e.type) ? new Blob([String.fromCharCode(65279), e], {
                            type: e.type
                        }) : e
                    },
                    c = function(c, f, s) {
                        s || (c = u(c));
                        var d, p = this,
                            v = "application/octet-stream" === c.type,
                            h = function() {
                                ! function(e, t, n) {
                                    for (var r = (t = [].concat(t)).length; r--;) {
                                        var o = e["on" + t[r]];
                                        if ("function" == typeof o) try {
                                            o.call(e, n || e)
                                        } catch (e) {
                                            i(e)
                                        }
                                    }
                                }(p, "writestart progress write writeend".split(" "))
                            };
                        if (p.readyState = p.INIT, r) return d = t().createObjectURL(c), void setTimeout(function() {
                            n.href = d, n.download = f,
                                function(e) {
                                    var t = new MouseEvent("click");
                                    e.dispatchEvent(t)
                                }(n), h(), a(d), p.readyState = p.DONE
                        });
                        ! function() {
                            if ((l || v && o) && e.FileReader) {
                                var n = new FileReader;
                                return n.onloadend = function() {
                                    var t = l ? n.result : n.result.replace(/^data:[^;]*;/, "data:attachment/file;");
                                    e.open(t, "_blank") || (e.location.href = t), t = void 0, p.readyState = p.DONE, h()
                                }, n.readAsDataURL(c), void(p.readyState = p.INIT)
                            }
                            d || (d = t().createObjectURL(c)), v ? e.location.href = d : e.open(d, "_blank") || (e.location.href = d);
                            p.readyState = p.DONE, h(), a(d)
                        }()
                    },
                    f = c.prototype;
                return "undefined" != typeof navigator && navigator.msSaveOrOpenBlob ? function(e, t, n) {
                    return t = t || e.name || "download", n || (e = u(e)), navigator.msSaveOrOpenBlob(e, t)
                } : (f.abort = function() {}, f.readyState = f.INIT = 0, f.WRITING = 1, f.DONE = 2, f.error = f.onwritestart = f.onprogress = f.onwrite = f.onabort = f.onerror = f.onwriteend = null, function(e, t, n) {
                    return new c(e, t || e.name || "download", n)
                })
            }
        }("undefined" != typeof self && self || "undefined" != typeof window && window || this.content);
        /*! @source http://purl.eligrey.com/github/FileSaver.js/blob/master/FileSaver.js */
        void 0 !== e && e.exports ? e.exports.saveAs = o : null !== n(7) && null !== n(8) && (void 0 === (r = function() {
            return o
        }.call(t, n, t, e)) || (e.exports = r))
    }, function(e, t) {
        e.exports = function() {
            throw new Error("define cannot be used indirect")
        }
    }, function(e, t) {
        (function(t) {
            e.exports = t
        }).call(this, {})
    }, function(e, t, n) {
        "use strict";
        Object.defineProperty(t, "__esModule", {
            value: !0
        });
        var r = Object.assign || function(e) {
                for (var t = 1; t < arguments.length; t++) {
                    var n = arguments[t];
                    for (var r in n) Object.prototype.hasOwnProperty.call(n, r) && (e[r] = n[r])
                }
                return e
            },
            o = n(0);
        t.default = {
            workcellCreated: function(e) {
                var t = e.workcell,
                    n = e.cellStyle,
                    l = n.fontWeight;
                t.font = r({}, t.font || {}, {
                    name: n.fontFamily,
                    color: {
                        argb: (0, o.argb)(n.color)
                    },
                    bold: "bold" === l || +l > 600
                })
            }
        }
    }, function(e, t, n) {
        "use strict";
        Object.defineProperty(t, "__esModule", {
            value: !0
        });
        var r = Object.assign || function(e) {
                for (var t = 1; t < arguments.length; t++) {
                    var n = arguments[t];
                    for (var r in n) Object.prototype.hasOwnProperty.call(n, r) && (e[r] = n[r])
                }
                return e
            },
            o = n(0);
        t.default = {
            workcellCreated: function(e) {
                var t = e.workcell,
                    n = e.cellStyle,
                    l = (0, o.argb)(n.backgroundColor);
                t.fill = r({}, t.fill || {}, "00000000" === l ? {
                    type: "pattern",
                    pattern: "none"
                } : {
                    type: "pattern",
                    pattern: "solid",
                    fgColor: {
                        argb: l
                    }
                })
            }
        }
    }, function(e, t, n) {
        "use strict";
        Object.defineProperty(t, "__esModule", {
            value: !0
        }), t.default = {
            workcellCreated: function(e) {
                var t = e.workcell,
                    n = e.cell.children[0];
                n && ["INPUT", "SELECT", "TEXTAREA"].includes(n.tagName) && (t.value = n.value)
            }
        }
    }, function(e, t, n) {
        "use strict";
        Object.defineProperty(t, "__esModule", {
            value: !0
        });
        var r = Object.assign || function(e) {
            for (var t = 1; t < arguments.length; t++) {
                var n = arguments[t];
                for (var r in n) Object.prototype.hasOwnProperty.call(n, r) && (e[r] = n[r])
            }
            return e
        };
        t.default = {
            workcellCreated: function(e) {
                var t = e.workcell,
                    n = e.cellStyle,
                    o = n.verticalAlign,
                    l = n.textAlign;
                t.alignment = r({}, t.alignment || {}, {
                    vertical: function() {
                        for (var e = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : "", t = ["top", "middle", "bottom"], n = 0; n < t.length; n++)
                            if (e.includes(t[n])) return t[n];
                        return {
                            baseline: "middle",
                            super: "top",
                            sub: "bottom"
                        } [e]
                    }(o),
                    horizontal: function() {
                        for (var e = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : "", t = ["right", "left", "center", "justify"], n = 0; n < t.length; n++)
                            if (e.includes(t[n])) return t[n]
                    }(l)
                })
            }
        }
    }, function(e, t, n) {
        "use strict";
        Object.defineProperty(t, "__esModule", {
            value: !0
        }), t.default = {
            workcellCreated: function(e) {
                var t = e.workcell,
                    n = e.cell.children[0];
                n && "A" === n.tagName && (t.value = {
                    text: n.innerText,
                    hyperlink: n.href
                })
            }
        }
    }, function(e, t, n) {
        "use strict";
        Object.defineProperty(t, "__esModule", {
            value: !0
        }), t.default = {
            workcellCreated: function(e) {
                var t = e.worksheet,
                    n = e.colRange,
                    r = (e.cell, e.cellStyle);
                n.from === n.to && "auto" !== r.width && (t.getColumn(n.from + 1).width = +r.width.split("px")[0] * this.options.widthRatio)
            }
        }
    }]).default
});