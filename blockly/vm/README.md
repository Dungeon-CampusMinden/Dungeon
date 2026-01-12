VM should use a generator (coroutine) to execute instructions. That way execution speed can be handled outside the VM itself.

Instructions will be passed in readable form for easier debugging and introspection.

Handle breakpoints as a separate table for lookup based on the instructions source position.

Include a source position as part of the instruction.

Update global state only through event driven mechanisms
