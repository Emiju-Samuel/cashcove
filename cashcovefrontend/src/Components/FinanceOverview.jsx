import React from 'react'
import CustomPieChart from './CustomPieChart';
import { addThousandsSeperator } from './util';

const FinanceOverview = ({totalBalance, totalIncome, totalExpense}) => {

    const COLORS = ["#591688", "#a0090", "#016630"];

    const balanceData = [
        {name: "Total Balance", amount: totalBalance},
        {name: "Total Expenses", amount: totalExpense},
        {name: "Total Income", amount: totalIncome},
    ];

  return (
    <div className="card">
        <div className="flex items-center justify-between">
            <h5 className="text-lg">Financial Overview</h5>
        </div>

        <CustomPieChart
            data={balanceData}
            label="Total Balance"
            totalAmount={`${addThousandsSeperator(totalBalance)}`}
            colors={COLORS}
            showTextAnchor
        />
    </div>
  )
}

export default FinanceOverview