import { Injectable } from "@angular/core";
import { Action, Selector, State, StateContext } from "@ngxs/store";
import { UserService } from "../services/user.service";
import { User } from "../models/user.model";
import { Users } from "./user.actions";
import { tap } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { getFinishedValueOrDefault, LoadingState, LoadingStateTag } from "./loading-state.model";
import { AuthService } from "../services/auth.service";
import { Auth } from "./auth.actions";

export interface AuthModel {
    username: string,
    password: string,
}

export interface FabxStateModel {
    auth: AuthModel | null,
    users: LoadingState<User[]>
}

@State<FabxStateModel>({
    name: 'fabx',
    defaults: {
        auth: null,
        users: { tag: "LOADING" },
    }
})
@Injectable()
export class FabxState {
    constructor(private authService: AuthService, private userService: UserService) {}

    // AUTH

    @Selector()
    static auth(state: FabxStateModel): AuthModel | null {
        return state.auth;
    }

    @Selector()
    static isAuthenticated(state: FabxStateModel): boolean {
        return Boolean(state.auth);
    }

    @Action(Auth.Login)
    login(ctx: StateContext<FabxStateModel>, action: Auth.Login) {
        return this.authService.login(action.payload.username, action.payload.password).pipe(
            tap({
                next: _ => {
                    ctx.patchState({
                        auth: { username: action.payload.username, password: action.payload.password }
                    });
                }
            })
        );
    }

    @Action(Auth.Logout)
    logout(ctx: StateContext<FabxStateModel>) {
        ctx.patchState({
            auth: null
        });
    }

    // USERS

    @Selector()
    static users(state: FabxStateModel): User[] {
        return getFinishedValueOrDefault(state.users, []);
    }

    @Selector()
    static usersLoadingState(state: FabxStateModel): LoadingStateTag {
        return state.users.tag;
    }

    @Action(Users.GetAll)
    getAllUsers(ctx: StateContext<FabxStateModel>) {
        ctx.patchState({
            users: { tag: "LOADING" }
        });

        return this.userService.getAllUsers().pipe(
            tap({
                next: value => {
                    ctx.patchState({
                        users: { tag: "FINISHED", value: value }
                    });
                },
                error: (err: HttpErrorResponse) => {
                    console.error("error while getting all users: {}", err);
                    ctx.patchState({
                        users: { tag: "ERROR", err: err }
                    });
                }
            })
        );
    }
}
