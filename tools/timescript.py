from github import Github

# Replace YOUR_ACCESS_TOKEN with your GitHub access token
g = Github("YOUR_ACCESS_TOKEN")

repo = g.get_repo("Programmiermethoden/Dungeon")

issues = repo.get_issues(state="open")


milestones = {}
unestimated_issues = {}

# Loop through all open issues in the repository
for issue in issues:
    if issue.milestone is not None:
        milestone = issue.milestone.title
    else:
        milestone = "no_milestone"
    
    # Check if the issue has a #Time tag in the description
    description = issue.body
    time_estimation = 0
    if description is not None and "#Time:" in description:
        time_estimation_str = description.split("#Time:")[1].split()[0]
        try:
            time_estimation = int(time_estimation_str)
        except ValueError:
            pass
    else:
        if milestone not in unestimated_issues:
            unestimated_issues[milestone] = 0
        unestimated_issues[milestone] += 1
    
    if milestone not in milestones:
        milestones[milestone] = {"count": 0, "effort": 0, "unestimated_issues": 0}
    
    milestones[milestone]["count"] += 1
    milestones[milestone]["effort"] += time_estimation

# Print the results
for milestone, data in milestones.items():
    print(f"Milestone: {milestone}, Issues: {data['count']}, Effort: {data['effort']}, Unestimated Issues: {unestimated_issues.get(milestone, 0)}")