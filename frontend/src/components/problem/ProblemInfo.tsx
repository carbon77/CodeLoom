import { Badge } from '../ui/Controls'
import ui from '../ui/ui.module.css'
import styles from './ProblemInfo.module.css'
import type { Difficulty, ProblemDetail } from "../../api/problems";
import MarkdownView from "./MarkdownView";
const difficultyColors: Record<Difficulty, "success" | "warning" | "error"> = {
  EASY: "success",
  MEDIUM: "warning",
  HARD: "error",
};
interface ProblemInfoProps {
  problem: ProblemDetail;
}
export default function ProblemInfo({ problem }: ProblemInfoProps) {
  return (<>
    <div className={styles.heading}>
      <h1 className={styles.title}>
        {problem.title}
      </h1>
      <Badge tone={difficultyColors[problem.difficulty]}>{problem.difficulty}</Badge>
    </div>
    {problem.topics.length > 0 && (<div className={[ui.stack, styles.topics].join(" ")}>
      {problem.topics.map((topic) => <Badge key={topic.id}>{topic.name}</Badge>)}
    </div>)}
    <MarkdownView>{problem.description}</MarkdownView>

    {problem.constraints &&
      (problem.constraints.executionTimeLimitMs != null ||
        problem.constraints.memoryUsageLimitMb != null) && (<>
          <hr className={styles.constraintsDivider} />
          <h2 className={styles.constraintsTitle}>
            Constraints
          </h2>
          <div className={[ui.stack, styles.constraints].join(" ")}>
            {problem.constraints.executionTimeLimitMs != null && (<p className={ui.body}>
              Time limit: {problem.constraints.executionTimeLimitMs} ms
            </p>)}
            {problem.constraints.memoryUsageLimitMb != null && (<p className={ui.body}>
              Memory limit: {problem.constraints.memoryUsageLimitMb} MB
            </p>)}
          </div>
        </>)}

    {problem.examples.length ? (<>
      <hr className={styles.examplesDivider} />
      <h2 className={styles.examplesTitle}>
        Examples
      </h2>
      {problem.examples.map((example, index) => (<div key={index} className={[ui.panel, styles.example].join(" ")}>
        <span className={[ui.subtitle, styles.exampleTitle].join(" ")}>
          Example {index + 1}
        </span>
        <div className={styles.input}>
          <strong>Input:</strong>
          {`\n${example.input}`}
        </div>
        <div className={styles.output}>
          <strong>Output:</strong>
          {`\n${example.expectedOutput}`}
        </div>
        {example.explanation && (<p className={[ui.body, styles.explanation].join(" ")}>
          {example.explanation}
        </p>)}
      </div>))}
    </>) : null}

    {problem.hints.length > 0 && (<>
      <h2 className={styles.hintsTitle}>
        Hints
      </h2>
      <div className={[ui.stack, styles.hints].join(" ")}>
        {problem.hints.map((hint, index) => (<details key={index} className={[ui.disclosure, styles.hint].join(" ")}>
          <summary className={ui.summary}>
            <span className={ui.subtitle}>Hint {index + 1}</span>
          </summary>
          <div className={ui.details}>
            <p className={ui.body}>{hint}</p>
          </div>
        </details>))}
      </div>
    </>)}
  </>);
}
