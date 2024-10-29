import classNames from 'classnames';

export default function SyncInfoLabel({children, className, color}) {
  const colorClasses = {
    blue: 'bg-blue-500',
    green: 'bg-green-500',
  };

  return (
    <div
      className={classNames(
        "px-2 rounded-md text-white flex items-center",
        colorClasses[color],
        className
      )}
    >
      {children}
    </div>
  );
}
