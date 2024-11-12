export default function Card({children}) {
  return (
    <div
      className="flex flex-col self-center w-1/3 p-8 gap-2 mx-auto bg-gray-50 shadow-lg">
      {children}
    </div>
  );
};