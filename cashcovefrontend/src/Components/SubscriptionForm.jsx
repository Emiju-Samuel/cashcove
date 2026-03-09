import { useEffect, useState } from 'react';
import { Calendar, AlertCircle } from 'lucide-react';

const SubscriptionForm = ({ initialData, categories, onSubmit, onCancel, isLoading }) => {
  const [formData, setFormData] = useState({
    icon: '📦',
    subscriptionName: '',
    amount: '',
    frequency: 'MONTHLY',
    startDate: new Date().toISOString().split('T')[0],
    nextRenewalDate: '',
    reminderDaysBefore: 1,
    categoryId: '',
  });

  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});

  // Calculate next renewal date based on start date and frequency
  const calculateNextRenewal = (startDate, frequency) => {
    if (!startDate || !frequency) return '';
    
    // Parse the date string (yyyy-MM-dd) manually to avoid timezone issues
    const [yearStr, monthStr, dayStr] = startDate.split('-');
    const year = parseInt(yearStr);
    const month = parseInt(monthStr); // 1-12
    const day = parseInt(dayStr);

    let newMonth = month;
    let newYear = year;

    // Add months based on frequency
    switch (frequency) {
      case 'MONTHLY':
        newMonth += 1;
        break;
      case 'QUARTERLY':
        newMonth += 3;
        break;
      case 'SEMI_ANNUAL':
        newMonth += 6;
        break;
      case 'YEARLY':
        newYear += 1;
        break;
      default:
        return startDate;
    }

    // Handle month overflow
    while (newMonth > 12) {
      newMonth -= 12;
      newYear += 1;
    }

    // Format as yyyy-MM-dd
    const formattedMonth = String(newMonth).padStart(2, '0');
    const formattedDay = String(day).padStart(2, '0');
    return `${newYear}-${formattedMonth}-${formattedDay}`;
  };

  useEffect(() => {
    if (initialData) {
      setFormData({
        icon: initialData.icon || '📦',
        subscriptionName: initialData.subscriptionName || '',
        amount: initialData.amount?.toString() || '',
        frequency: initialData.frequency || 'MONTHLY',
        startDate: initialData.startDate || new Date().toISOString().split('T')[0],
        nextRenewalDate: initialData.nextRenewalDate || '',
        reminderDaysBefore: initialData.reminderDaysBefore || 1,
        categoryId: initialData.categoryId?.toString() || '',
      });
    }
  }, [initialData]);

  // Auto-calculate nextRenewalDate when startDate or frequency changes
  useEffect(() => {
    if (formData.startDate && formData.frequency) {
      const calculated = calculateNextRenewal(formData.startDate, formData.frequency);
      setFormData((prev) => ({
        ...prev,
        nextRenewalDate: calculated,
      }));
    }
  }, [formData.startDate, formData.frequency]);

  const validateForm = () => {
    const newErrors = {};

    if (!formData.subscriptionName.trim()) {
      newErrors.subscriptionName = 'Subscription name is required';
    }

    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      newErrors.amount = 'Enter a valid amount greater than 0';
    }

    if (!formData.categoryId) {
      newErrors.categoryId = 'Please select a category';
    }

    // nextRenewalDate is calculated by the backend, no need to validate here

    if (formData.reminderDaysBefore < 1 || formData.reminderDaysBefore > 30) {
      newErrors.reminderDaysBefore = 'Reminder must be between 1 and 30 days';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleBlur = (e) => {
    const { name } = e.target;
    setTouched((prev) => ({
      ...prev,
      [name]: true,
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validateForm()) {
      onSubmit({
        icon: formData.icon,
        subscriptionName: formData.subscriptionName,
        amount: parseFloat(formData.amount),
        frequency: formData.frequency,
        startDate: formData.startDate,
        reminderDaysBefore: parseInt(formData.reminderDaysBefore),
        categoryId: parseInt(formData.categoryId),
      });
    }
  };

  const formatCurrency = (value) => {
    if (!value) return '₦0';
    return new Intl.NumberFormat('en-NG', {
      style: 'currency',
      currency: 'NGN',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

  const getFrequencyLabel = (freq) => {
    const labels = {
      MONTHLY: 'per month',
      QUARTERLY: 'per 3 months',
      SEMI_ANNUAL: 'per 6 months',
      YEARLY: 'per year',
    };
    return labels[freq] || 'per month';
  };

  const formField = (label, name, type = 'text', helper = '') => (
    <div className="mb-4">
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      <input
        type={type}
        name={name}
        value={formData[name]}
        onChange={handleChange}
        onBlur={handleBlur}
        className={`w-full px-4 py-2 rounded-lg border ${
          touched[name] && errors[name]
            ? 'border-red-500 bg-red-50'
            : 'border-gray-300 bg-white'
        } focus:outline-none focus:ring-2 focus:ring-blue-500`}
        disabled={isLoading}
      />
      {helper && <p className="text-xs text-gray-500 mt-1">{helper}</p>}
      {touched[name] && errors[name] && (
        <div className="flex items-center gap-1 text-red-600 text-xs mt-1">
          <AlertCircle size={14} /> {errors[name]}
        </div>
      )}
    </div>
  );

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Emoji Picker */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-2">Icon</label>
        <div className="grid grid-cols-5 lg:grid-cols-10 gap-2">
          {['📦', '💰', '🎬', '🎵', '📱', '🏋️', '☕', '🍔', '🚗', '🏠'].map((emoji) => (
            <button
              key={emoji}
              type="button"
              onClick={() => setFormData((prev) => ({ ...prev, icon: emoji }))}
              className={`text-2xl p-2 rounded-lg transition ${
                formData.icon === emoji ? 'bg-blue-100 ring-2 ring-blue-500' : 'bg-gray-100 hover:bg-gray-200'
              }`}
            >
              {emoji}
            </button>
          ))}
        </div>
      </div>

      {/* Subscription Name */}
      {formField('Subscription Name', 'subscriptionName', 'text', 'e.g., Netflix, Disney+, Spotify, Gym membership')}

      {/* Amount and Frequency */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          {formField('Amount', 'amount', 'number', 'Enter amount in ₦')}
        </div>
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">Frequency</label>
          <select
            name="frequency"
            value={formData.frequency}
            onChange={handleChange}
            onBlur={handleBlur}
            className="w-full px-4 py-2 rounded-lg border border-gray-300 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={isLoading}
          >
            <option value="MONTHLY">Monthly</option>
            <option value="QUARTERLY">Quarterly</option>
            <option value="SEMI_ANNUAL">Semi-Annual</option>
            <option value="YEARLY">Yearly</option>
          </select>
        </div>
      </div>

      {/* Category */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-1">Category</label>
        <select
          name="categoryId"
          value={formData.categoryId}
          onChange={handleChange}
          onBlur={handleBlur}
          className={`w-full px-4 py-2 rounded-lg border ${
            touched.categoryId && errors.categoryId
              ? 'border-red-500 bg-red-50'
              : 'border-gray-300 bg-white'
          } focus:outline-none focus:ring-2 focus:ring-blue-500`}
          disabled={isLoading}
        >
          <option value="">Select a category</option>
          {categories.map((cat) => (
            <option key={cat.id} value={cat.id}>
              {cat.icon} {cat.name}
            </option>
          ))}
        </select>
        {touched.categoryId && errors.categoryId && (
          <div className="flex items-center gap-1 text-red-600 text-xs mt-1">
            <AlertCircle size={14} /> {errors.categoryId}
          </div>
        )}
      </div>

      {/* Dates */}
      <div className="grid grid-cols-2 gap-4">
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">Start Date</label>
          <div className="relative">
            <Calendar className="absolute left-3 top-3 text-gray-400" size={18} />
            <input
              type="date"
              name="startDate"
              value={formData.startDate}
              onChange={handleChange}
              onBlur={handleBlur}
              className="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-300 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              disabled={isLoading}
            />
          </div>
        </div>
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">Next Renewal</label>
          <div className="relative">
            <Calendar className="absolute left-3 top-3 text-gray-400" size={18} />
            <input
              type="date"
              name="nextRenewalDate"
              value={formData.nextRenewalDate}
              onChange={handleChange}
              onBlur={handleBlur}
              className={`w-full pl-10 pr-4 py-2 rounded-lg border ${
                touched.nextRenewalDate && errors.nextRenewalDate
                  ? 'border-red-500 bg-red-50'
                  : 'border-gray-300 bg-white'
              } focus:outline-none focus:ring-2 focus:ring-blue-500`}
              disabled={isLoading}
            />
          </div>
          {touched.nextRenewalDate && errors.nextRenewalDate && (
            <div className="flex items-center gap-1 text-red-600 text-xs mt-1">
              <AlertCircle size={14} /> {errors.nextRenewalDate}
            </div>
          )}
        </div>
      </div>

      {/* Reminder */}
      {formField(
        'Remind Me (days before)',
        'reminderDaysBefore',
        'number',
        'Get notified before renewal'
      )}

      {/* Live Preview */}
      {formData.amount && formData.subscriptionName && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <p className="text-sm text-gray-600 mb-2">Preview:</p>
          <div className="flex items-baseline gap-2">
            <span className="text-2xl font-bold text-gray-900">
              {formatCurrency(formData.amount)}
            </span>
            <span className="text-sm text-gray-600">{getFrequencyLabel(formData.frequency)}</span>
          </div>
          <p className="text-xs text-gray-600 mt-2">
            💬 Next renewal: {new Date(formData.nextRenewalDate).toLocaleDateString('en-NG', {
              year: 'numeric',
              month: 'short',
              day: 'numeric',
            })} • {formData.icon} {formData.subscriptionName}
          </p>
        </div>
      )}

      {/* Actions */}
      <div className="flex gap-3 pt-4 border-t border-gray-200">
        <button
          type="button"
          onClick={onCancel}
          className="flex-1 px-4 py-2 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold rounded-lg transition"
          disabled={isLoading}
        >
          Cancel
        </button>
        <button
          type="submit"
          className="flex-1 px-4 py-2 bg-green-600 hover:bg-green-700 text-white font-semibold rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed"
          disabled={isLoading}
        >
          {isLoading ? 'Saving...' : initialData ? 'Update' : 'Add'} Subscription
        </button>
      </div>
    </form>
  );
};

export default SubscriptionForm;
