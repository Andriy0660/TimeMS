import Worklog from "./Worklog.jsx";

export default function WorklogList({worklogs, onDelete, onCreate}) {
  return (
    <div className="m-4">
      <div className="mb-2 p-2 bg-red-100 font-medium text-amber-900 w-fit rounded-2xl">Not synced worklogs</div>
      <div className="flex flex-col items-center">
        <div className="w-full overflow-x-auto">
          {worklogs.map(worklog => <Worklog key={worklog.id} worklog={worklog} onDelete={onDelete} onCreate={onCreate}/>)}
        </div>
      </div>
    </div>
  );
}