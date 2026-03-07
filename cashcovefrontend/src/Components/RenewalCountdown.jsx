import { useEffect, useState } from 'react';

const RenewalCountdown = ({ nextRenewalDate }) => {
  const [daysLeft, setDaysLeft] = useState(0);
  const [hoursLeft, setHoursLeft] = useState(0);

  useEffect(() => {
    const calculateCountdown = () => {
      const now = new Date();
      const renewal = new Date(nextRenewalDate);
      const diff = renewal - now;

      if (diff <= 0) {
        setDaysLeft(0);
        setHoursLeft(0);
        return;
      }

      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));

      setDaysLeft(days);
      setHoursLeft(hours);
    };

    calculateCountdown();
    const interval = setInterval(calculateCountdown, 60000); // Update every minute

    return () => clearInterval(interval);
  }, [nextRenewalDate]);

  // Determine color based on days left
  const getColor = () => {
    if (daysLeft > 7) return { ring: "ring-green-300", text: "text-green-700", bg: "bg-green-50" };
    if (daysLeft > 3) return { ring: "ring-yellow-300", text: "text-yellow-700", bg: "bg-yellow-50" };
    return { ring: "ring-red-300", text: "text-red-700", bg: "bg-red-50" };
  };

  const color = getColor();

  return (
    <div className={`flex items-center gap-2 px-3 py-2 rounded-lg ring-1 ${color.ring} ${color.bg}`}>
      <svg
        className={`w-5 h-5 ${color.text}`}
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
      >
        <circle cx="12" cy="12" r="9" />
        <polyline points="12 6 12 12 16 14" />
      </svg>
      <div className="text-sm font-semibold">
        <div className={color.text}>
          {daysLeft > 0 ? `${daysLeft}d` : `${hoursLeft}h`}
        </div>
        {daysLeft === 0 && hoursLeft === 0 && (
          <div className={`text-xs ${color.text}`}>Today</div>
        )}
      </div>
    </div>
  );
};

export default RenewalCountdown;
