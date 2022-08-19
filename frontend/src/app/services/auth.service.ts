import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from "@angular/common/http";
import { catchError, Observable, throwError } from "rxjs";
import { environment } from "../../environments/environment";
import { Store } from "@ngxs/store";
import { FabxState } from "../state/fabx-state";
import { TokenResponse } from "../models/user.model";

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private baseUrl = environment.baseUrl;

    constructor(private http: HttpClient, private store: Store) {}

    login(username: string, password: string): Observable<TokenResponse> {
        return this.http.get<TokenResponse>(`${this.baseUrl}/login`, {
            headers: new HttpHeaders({
                "Authorization": "Basic " + btoa(`${username}:${password}`)
            })
        }).pipe(
            catchError((err: HttpErrorResponse, _) => {
                console.error("login error: %o", err)
                return throwError(() => err)
            })
        );
    }

    loginWebauthn(username: string) {
        console.log("loginWebauthn", username);

        this.http.post<WebauthnLoginResponse>(
            `${this.baseUrl}/webauthn/login`,
            {
                'username': username
            }
        ).subscribe({
            next: value => {
                const userId = value.userId;

                const allowCred: PublicKeyCredentialDescriptor[] = value.credentialIds.map(e => {
                    const eArr = new Int8Array(e.values());
                    return {
                        id: eArr.buffer,
                        type: "public-key"
                    }
                });

                const challengeArray = new Int8Array(value.challenge);

                navigator.credentials.get({
                    publicKey: {
                        allowCredentials: allowCred,
                        challenge: challengeArray.buffer
                    }
                }).then((response) => {
                    console.log("response", response);

                    if (response) {
                        const pkc = response as PublicKeyCredential
                        const r = pkc.response as AuthenticatorAssertionResponse

                        const credentialIdArray = new Int8Array(pkc.rawId)
                        const authenticatorDataArray = new Int8Array(r.authenticatorData)
                        const clientDataJSONArray = new Int8Array(r.clientDataJSON)
                        const signatureArray = new Int8Array(r.signature)

                        const responseDetails: WebauthnResponseDetails = {
                            userId: userId,
                            credentialId: Array.from(credentialIdArray),
                            authenticatorData: Array.from(authenticatorDataArray),
                            clientDataJSON: Array.from(clientDataJSONArray),
                            signature: Array.from(signatureArray),
                        }

                        this.http.post<TokenResponse>(
                            `${this.baseUrl}/webauthn/response`,
                            responseDetails
                        ).subscribe({
                            next: value => {
                                console.log("value", value);
                            }
                        });
                    }
                });
            }
        })
    }

    getOptions() {
        let auth = this.store.selectSnapshot(FabxState.auth);

        if (auth) {
            return {
                headers: new HttpHeaders({
                    "Authorization": `Bearer ${auth.token}`
                })
            };
        } else {
            return {};
        }
    }
}

export interface WebauthnLoginResponse {
    userId: string,
    challenge: number[],
    credentialIds: number[][]
}

export interface WebauthnResponseDetails {
    userId: string,
    credentialId: number[],
    authenticatorData: number[],
    clientDataJSON: number[],
    signature: number[]
}
