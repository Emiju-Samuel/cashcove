import { Plus } from 'lucide-react';
import React from 'react';
import { useEffect, useState } from 'react'
import { prepareIncomeLineChartData, formatDate, formatAmount, generateSmoothPath } from './incomeChartUtils';

// Custom Interactive Line Chart Component
const CustomLineChart = ({ data }) => {
    const [hoveredPoint, setHoveredPoint] = useState(null);

    if (!data || data.length === 0) {
        return <div className="text-center text-gray-500 py-10">No income data available</div>;
    }

    // Calculate chart dimensions and scale
    const chartWidth = 800;
    const chartHeight = 500;
    const padding = { top: 50, right: 50, bottom: 100, left: 100 };
    const plotWidth = chartWidth - padding.left - padding.right;
    const plotHeight = chartHeight - padding.top - padding.bottom;

    const maxAmount = Math.max(...data.map(d => d.amount));
    const minAmount = 0;

    // Scale functions
    const scaleX = (index) => padding.left + (index / (data.length - 1 || 1)) * plotWidth;
    const scaleY = (amount) => padding.top + plotHeight - ((amount - minAmount) / (maxAmount - minAmount || 1)) * plotHeight;

    return (
        <div className="flex justify-center">
            <svg width={chartWidth} height={chartHeight} className="border border-gray-300 rounded bg-white px-3">
                {/* Grid lines */}
                {[0, 0.25, 0.5, 0.75, 1].map((ratio) => (
                    <line
                        key={`hgrid-${ratio}`}
                        x1={padding.left}
                        y1={padding.top + plotHeight * (1 - ratio)}
                        x2={chartWidth - padding.right}
                        y2={padding.top + plotHeight * (1 - ratio)}
                        stroke="#e5e7eb"
                        strokeDasharray="4"
                        strokeWidth="1"
                    />
                ))}

                {/* Y-axis */}
                <line
                    x1={padding.left}
                    y1={padding.top}
                    x2={padding.left}
                    y2={chartHeight - padding.bottom}
                    stroke="#000"
                    strokeWidth="2"
                />

                {/* X-axis */}
                <line
                    x1={padding.left}
                    y1={chartHeight - padding.bottom}
                    x2={chartWidth - padding.right}
                    y2={chartHeight - padding.bottom}
                    stroke="#000"
                    strokeWidth="2"
                />

                {/* Y-axis labels */}
                {[0, 0.25, 0.5, 0.75, 1].map((ratio) => {
                    const amount = minAmount + (maxAmount - minAmount) * ratio;
                    return (
                        <text
                            key={`ylabel-${ratio}`}
                            x={padding.left - 10}
                            y={padding.top + plotHeight * (1 - ratio) + 4}
                            textAnchor="end"
                            fontSize="12"
                            fill="#666"
                        >
                            {formatAmount(amount)}
                        </text>
                    );
                })}

                {/* X-axis labels */}
                {data.map((d, i) => (
                    data.length <= 12 || i % Math.ceil(data.length / 6) === 0) && (
                        <text
                            key={`xlabel-${i}`}
                            x={scaleX(i)}
                            y={chartHeight - padding.bottom + 20}
                            textAnchor="middle"
                            fontSize="12"
                            fill="#666"
                        >
                            {formatDate(d.date)}
                        </text>
                    )
                )}

                {/* Smooth curved path */}
                <path
                    d={generateSmoothPath(data, scaleX, scaleY)}
                    fill="none"
                    stroke="#f59e0b"
                    strokeWidth="3"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                />

                {/* Data points and interactive hover circles */}
                {data.map((d, i) => (
                    <g
                        key={`point-${i}`}
                        onMouseEnter={() => setHoveredPoint(i)}
                        onMouseLeave={() => setHoveredPoint(null)}
                        className="cursor-pointer"
                    >
                        <circle
                            cx={scaleX(i)}
                            cy={scaleY(d.amount)}
                            r={hoveredPoint === i ? 8 : 5}
                            fill={hoveredPoint === i ? '#d97706' : '#f59e0b'}
                            opacity={hoveredPoint === i ? 1 : 0.7}
                            className="transition-all"
                        />

                        {/* Tooltip on hover */}
                        {hoveredPoint === i && (
                            <g>
                                <rect
                                    x={scaleX(i) - 60}
                                    y={scaleY(d.amount) - 50}
                                    width="120"
                                    height="40"
                                    rx="4"
                                    fill="#1f2937"
                                    opacity="0.9"
                                />
                                <text
                                    x={scaleX(i)}
                                    y={scaleY(d.amount) - 30}
                                    textAnchor="middle"
                                    fontSize="12"
                                    fill="#fff"
                                    fontWeight="bold"
                                >
                                    {formatAmount(d.amount)}
                                </text>
                                <text
                                    x={scaleX(i)}
                                    y={scaleY(d.amount) - 15}
                                    textAnchor="middle"
                                    fontSize="11"
                                    fill="#9ca3af"
                                >
                                    {formatDate(d.date)}
                                </text>
                            </g>
                        )}
                    </g>
                ))}

                {/* Y-axis label
                <text
                    x={-chartHeight / 2}
                    y={15}
                    textAnchor="middle"
                    fontSize="13"
                    fill="#666"
                    transform="rotate(-90)"
                    fontWeight="500"
                >
                   
                </text> */}

                {/* X-axis label */}
                {/* <text
                    x={chartWidth / 2}
                    y={chartHeight - 5}
                    textAnchor="middle"
                    fontSize="13"
                    fill="#666"
                    fontWeight="500"
                >
                    
                </text> */}
            </svg>
        </div>
    );
};

const IncomeOverview = ({transactions, onAddIncome}) => {

    const [chartData, setChartData] = useState([]);

    useEffect(()=>{
        const result = prepareIncomeLineChartData(transactions);
        console.log(result);
        setChartData(result);

        return () => {};
    }, [transactions]);

  return (
    <div className="card bg-amber-100 shadow p-3">
        <div className="flex items-center justify-between">
            <div>
                <h5 className="text-lg">
                Income Overview
            </h5>
            <p className="text-xs text-gray-400 mt-0 5">
                Track your earnings over time and analyze your income trends
            </p>
            </div>
            <button className='add-btn bg-white rounded-xs px-2 py-1 flex items-center' onClick={onAddIncome}>
              <Plus size={15} className='text-lg'/> Add Income
            </button>
        </div>

        <div className="mt-10">
            {/* create line chart */}
            <CustomLineChart data={chartData}/>
        </div>
    </div>
  )
}

export default IncomeOverview