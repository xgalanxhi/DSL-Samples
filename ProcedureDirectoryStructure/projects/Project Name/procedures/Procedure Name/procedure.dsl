
procedure 'Procedure Name', {
  projectName = 'Project Name'
  timeLimitUnits = 'minutes'

  formalOutputParameter 'output'

  formalOutputParameter 'outputGroovy'

  formalParameter 'input', defaultValue: 'input default', {
    expansionDeferred = '0'
    required = '0'
  }
}
