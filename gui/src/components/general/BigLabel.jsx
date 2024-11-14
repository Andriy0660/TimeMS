import classNames from "classnames";

export default function BigLabel({children, className, color="blue"}) {
  const colorClasses = {
    blue: "text-blue-500",
    green: "text-green-500",
  };

  return (
    <div
      className={classNames(
        "flex items-center w-fit p-2 border border-solid text-4xl rounded-xl shadow-md",
        colorClasses[color],
        className
      )}
    >
      {children}
    </div>
  );
}
