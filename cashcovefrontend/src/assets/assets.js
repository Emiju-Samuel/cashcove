import logo from "../assets/logo.png";
import loginBg from "../assets/loginbg.jpg";
import { CalendarCheck, Coins, FunnelPlus, LayoutDashboard, List, Wallet } from "lucide-react";


export const assets = {
    logo,
    loginBg,
}

export const SIDE_BAR_DATA = [

    {
        id: "01",
        label: "Dashboard",
        icon: LayoutDashboard,
        path: "/dashboard"
    },
    {
        id: "02",
        label: "Category",
        icon: List,
        path: "/category"
    },
    {
        id: "03",
        label: "Income",
        icon: Wallet,
        path: "/income"
    },
    {
        id: "04",
        label: "Expenses",
        icon: Coins,
        path: "/expense"
    },
    {
        id:"05",
        label: "Filters",
        icon: FunnelPlus,
        path: "/filter"
    },
    {
        id: "06",
        label: "Subscriptions",
        icon: CalendarCheck,
        path: "/subscription"
    }

]