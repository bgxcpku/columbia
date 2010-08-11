%gn = add_call_counter(@(x) sqrt(1-x.^2));
gn = @testfn;
x = 4*quadl(gn, 0, 1, 0); gn(), fprintf('%10.10g\n', x, pi), (x-pi)
x = 4*quadl(gn, 0, 1, 1e-14); gn(), fprintf('%10.10g\n', x, pi), (x-pi)
x = 4*quadl(gn, 0, 1, 1e-12); gn(), fprintf('%10.10g\n', x, pi), (x-pi)
x = 4*quadl(gn, 0, 1, 1e-9); gn(), fprintf('%10.10g\n', x, pi), (x-pi)
x = 4*quadl(gn, 0, 1, 1e-6); gn(), fprintf('%10.10g\n', x, pi), (x-pi)
